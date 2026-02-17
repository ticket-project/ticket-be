package com.ticket.core.domain.reservation;

import com.ticket.core.domain.performance.Performance;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationValidator {

    private final ReservationDetailRepository reservationDetailRepository;

    public void validateNew(final int reserveRequestSeatSize,
                            final Long memberId,
                            final Performance performance,
                            final int canReservePerformanceSeatsSize) {
        final long reservedCount = reservationDetailRepository.countByMemberIdAndPerformanceId(memberId, performance.getId());

        if (performance.isOverCount(reservedCount + reserveRequestSeatSize)) {
            throw new CoreException(ErrorType.EXCEED_AVAILABLE_SEATS);
        }

        if (canReservePerformanceSeatsSize != reserveRequestSeatSize) {
            throw new CoreException(ErrorType.SEAT_COUNT_MISMATCH);
        }
    }
}
