package com.ticket.core.api.controller.v1.response;

public class MemberResponse {
    private final Long memberId;
    private final String email;
    private final String name;

    public MemberResponse(final Long memberId, final String email, final String name) {
        this.memberId = memberId;
        this.email = email;
        this.name = name;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }
}
