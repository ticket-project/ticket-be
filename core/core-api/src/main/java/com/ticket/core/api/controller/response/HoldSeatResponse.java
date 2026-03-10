package com.ticket.core.api.controller.response;

import java.math.BigDecimal;
import java.util.List;

public record HoldSeatResponse(
        Long orderId,
        String orderNo,
        BigDecimal totalAmount,
        List<SeatInfo> seats
) {
    public record SeatInfo(Long performanceSeatId, BigDecimal price) {}
}
