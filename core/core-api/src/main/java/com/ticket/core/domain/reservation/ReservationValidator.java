package com.ticket.core.domain.reservation;

import com.ticket.core.domain.member.Member;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import com.ticket.storage.db.core.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReservationValidator {

    private final ReservationDetailRepository reservationDetailRepository;

    public ReservationValidator(final ReservationDetailRepository reservationDetailRepository) {
        this.reservationDetailRepository = reservationDetailRepository;
    }

    public void validateNew(final NewReservation newReservation,
                            final Member member,
                            final PerformanceEntity performance,
                            final List<PerformanceSeatEntity> performanceSeats) {
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
    }
}
