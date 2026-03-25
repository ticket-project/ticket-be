package com.ticket.core.domain.order.create;

import com.ticket.core.domain.hold.model.HoldSnapshot;
import com.ticket.core.domain.hold.support.HoldManager;
import com.ticket.core.domain.hold.support.HoldSeatAvailabilityValidator;
import com.ticket.core.domain.performanceseat.model.PerformanceSeat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class HoldAllocator {

    private final HoldSeatAvailabilityValidator holdSeatAvailabilityValidator;
    private final HoldManager holdManager;

    public HoldAllocation allocate(
            final Long memberId,
            final Long performanceId,
            final RequestedSeatIds requestedSeatIds,
            final Duration holdDuration,
            final LocalDateTime now
    ) {
        final List<PerformanceSeat> seats = holdSeatAvailabilityValidator.validate(performanceId, requestedSeatIds);
        final HoldSnapshot snapshot = holdManager.createHold(memberId, performanceId, requestedSeatIds, holdDuration, now);
        return new HoldAllocation(snapshot, seats);
    }

    public void release(final HoldAllocation allocation) {
        holdManager.release(
                allocation.snapshot().performanceId(),
                allocation.snapshot().holdKey(),
                allocation.snapshot().seatIds()
        );
    }
}
