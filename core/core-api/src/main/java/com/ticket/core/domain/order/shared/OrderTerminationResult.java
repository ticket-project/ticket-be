package com.ticket.core.domain.order.shared;

import java.util.List;

public record OrderTerminationResult(
        Long performanceId,
        String holdKey,
        List<Long> seatIds
) {
}
