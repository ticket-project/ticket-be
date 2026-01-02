package com.ticket.core.domain.hold;

import java.util.UUID;

public class HoldToken {
    private final Long memberId;
    private final String token;

    public HoldToken(final Long memberId, final String token) {
        this.memberId = memberId;
        this.token = token;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getToken() {
        return token;
    }

    public static HoldToken issue(final Long memberId) {
        return new HoldToken(memberId, UUID.randomUUID().toString());
    }
}
