package com.ticket.core.domain.order.event;

import java.util.List;

public record OrderExpiredEvent(Long performanceId, String holdToken, List<Long> seatIds) {
}
