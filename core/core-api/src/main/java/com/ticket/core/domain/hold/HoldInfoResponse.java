package com.ticket.core.domain.hold;

public class HoldInfoResponse {
    private final Long memberId;
    private final String token;

    public HoldInfoResponse(final Long memberId, final String token) {
        this.memberId = memberId;
        this.token = token;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getToken() {
        return token;
    }

    public static HoldInfoResponse from(final HoldToken holdToken) {
        return new HoldInfoResponse(holdToken.getMemberId(), holdToken.getToken());
    }

}
