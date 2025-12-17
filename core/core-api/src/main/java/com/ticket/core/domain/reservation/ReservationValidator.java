package com.ticket.core.domain.reservation;

import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.seat.SeatIds;
import com.ticket.core.enums.EntityStatus;
import com.ticket.core.enums.PerformanceSeatState;
import com.ticket.core.enums.PerformanceState;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import com.ticket.core.support.exception.NotFoundException;
import com.ticket.storage.db.core.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReservationValidator {

    private final ReservationDetailRepository reservationDetailRepository;
    private final PerformanceRepository performanceRepository;
    private final PerformanceSeatRepository performanceSeatRepository;


    public ReservationValidator(final ReservationDetailRepository reservationDetailRepository, final PerformanceRepository performanceRepository, final PerformanceSeatRepository performanceSeatRepository) {
        this.reservationDetailRepository = reservationDetailRepository;
        this.performanceRepository = performanceRepository;
        this.performanceSeatRepository = performanceSeatRepository;
    }

    public ReservationKey validateNew(final Member member,
                            final NewReservation newReservation) {
        final PerformanceEntity performance = findPerformance(newReservation.getPerformanceId());
        final List<PerformanceSeatEntity> performanceSeats = findPerformanceSeats(
                newReservation.getSeatIds(),
                performance.getId()
        );

        final int reserveRequestSeatSize = newReservation.getSeatIds().size();
        if (reserveRequestSeatSize > performance.getMaxCanReserveCount()) {
            throw new CoreException(ErrorType.EXCEED_AVAILABLE_SEATS);
        }

        final long reservedCount = reservationDetailRepository.countByMemberIdAndPerformanceId(member.getId(), performance.getId());

        if (performance.isOverCount(reservedCount + reserveRequestSeatSize)) {
            throw new CoreException(ErrorType.EXCEED_AVAILABLE_SEATS);
        }

        if (performanceSeats.size() != reserveRequestSeatSize) {
            throw new CoreException(ErrorType.SEAT_COUNT_MISMATCH);
        }

        return new ReservationKey(performance.getId(), performanceSeats);
    }

    private PerformanceEntity findPerformance(final Long performanceId) {
        return performanceRepository.findByIdAndStateAndStatus(
                        performanceId,
                        PerformanceState.OPEN,
                        EntityStatus.ACTIVE
                )
                .orElseThrow(() -> new NotFoundException(ErrorType.NOT_FOUND_DATA));
    }

    private List<PerformanceSeatEntity> findPerformanceSeats(final SeatIds seatIds, final Long performanceId) {
        return performanceSeatRepository.findByPerformanceIdAndSeatIdInAndState(
                performanceId,
                seatIds.getValues(),
                PerformanceSeatState.AVAILABLE
        );
    }
}
