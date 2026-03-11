package com.ticket.core.domain.order.event;

import java.util.List;

public record OrderCancelledEvent(Long performanceId, String holdToken, List<Long> seatIds) {
}
