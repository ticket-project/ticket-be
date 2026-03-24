package com.ticket.core.domain.order.create;

import com.ticket.core.domain.hold.model.HoldSnapshot;
import com.ticket.core.domain.performanceseat.model.PerformanceSeat;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public record HoldAllocation(
        HoldSnapshot snapshot,
        List<PerformanceSeat> performanceSeats
) {

    public String holdKey() {
        return snapshot.holdKey();
    }

    public LocalDateTime expiresAt() {
        return snapshot.expiresAt();
    }

    public LocalDateTime startedAt(final Duration holdDuration) {
        return snapshot.expiresAt().minus(holdDuration);
    }
}
