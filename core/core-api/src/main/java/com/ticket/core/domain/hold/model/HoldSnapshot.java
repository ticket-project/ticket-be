package com.ticket.core.domain.hold.model;

import java.time.LocalDateTime;
import java.util.List;

public record HoldSnapshot(
        String holdKey,
        Long memberId,
        Long performanceId,
        List<Long> seatIds,
        LocalDateTime expiresAt
) {
}
