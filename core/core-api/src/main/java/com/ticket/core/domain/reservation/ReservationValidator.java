package com.ticket.core.domain.reservation;

import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import com.ticket.storage.db.core.PerformanceEntity;
import com.ticket.storage.db.core.ReservationDetailRepository;
import org.springframework.stereotype.Component;

@Component
public class ReservationValidator {

    private final ReservationDetailRepository reservationDetailRepository;

    public ReservationValidator(final ReservationDetailRepository reservationDetailRepository) {
        this.reservationDetailRepository = reservationDetailRepository;
    }

    public void validateNew(final int reserveRequestSeatSize,
                            final Long memberId,
                            final PerformanceEntity performance,
                            final int canReservePerformanceSeatsSize) {
        final long reservedCount = reservationDetailRepository.countByMemberIdAndPerformanceId(memberId, performance.getId());

        if (performance.isOverCount(reservedCount + reserveRequestSeatSize) || performance.isOverCount(reserveRequestSeatSize)) {
            throw new CoreException(ErrorType.EXCEED_AVAILABLE_SEATS);
        }

        if (canReservePerformanceSeatsSize != reserveRequestSeatSize) {
            throw new CoreException(ErrorType.SEAT_COUNT_MISMATCH);
        }
    }
}
