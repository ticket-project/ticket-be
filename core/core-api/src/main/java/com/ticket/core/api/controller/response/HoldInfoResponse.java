package com.ticket.core.api.controller.response;

import com.ticket.core.domain.hold.HoldInfo;

import java.time.LocalDateTime;
import java.util.List;

public class HoldInfoResponse {
    private final Long memberId;
    private final Long performanceId;
    private final List<Long> seatIds;
    private final LocalDateTime expiredAt;

    public HoldInfoResponse(final Long memberId, final Long performanceId, final List<Long> seatIds, final LocalDateTime expiredAt) {
        this.memberId = memberId;
        this.performanceId = performanceId;
        this.seatIds = seatIds;
        this.expiredAt = expiredAt;
    }

    public static HoldInfoResponse from(final HoldInfo holdInfo) {
        return new HoldInfoResponse(holdInfo.getMemberId(), holdInfo.getPerformanceId(), holdInfo.getSeatIds(), holdInfo.getExpiredAt());
    }

    public Long getMemberId() {
        return memberId;
    }

    public Long getPerformanceId() {
        return performanceId;
    }

    public List<Long> getSeatIds() {
        return seatIds;
    }

    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }
}
