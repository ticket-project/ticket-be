package com.ticket.core.api.controller.response;

import com.ticket.core.domain.hold.Hold;

import java.time.LocalDateTime;

public class HoldInfoResponse {
    private final Long holdId;
    private final Long memberId;
    private final LocalDateTime expireAt;

    public HoldInfoResponse(final Long holdId, final Long memberId, final LocalDateTime expireAt) {
        this.holdId = holdId;
        this.memberId = memberId;
        this.expireAt = expireAt;
    }

    public static HoldInfoResponse from(final Hold hold) {
        return new HoldInfoResponse(hold.getId(), hold.getMember().getId(), hold.getExpireAt());
    }

    public Long getHoldId() {
        return holdId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public LocalDateTime getExpireAt() {
        return expireAt;
    }
}
