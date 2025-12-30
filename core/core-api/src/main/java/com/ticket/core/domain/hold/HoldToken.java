package com.ticket.core.domain.hold;

import java.util.UUID;

public class HoldToken {
    private final Long memberId;
    private final String token;

    public HoldToken(final Long memberId) {
        this.memberId = memberId;
        this.token = UUID.randomUUID().toString();
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getToken() {
        return token;
    }
}
