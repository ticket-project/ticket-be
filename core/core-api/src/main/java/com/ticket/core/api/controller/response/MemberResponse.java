package com.ticket.core.api.controller.response;

public class MemberResponse {
    private final Long memberId;
    private final String email;
    private final String name;
    private final String role;

    public MemberResponse(final Long memberId, final String email, final String name, final String role) {
        this.memberId = memberId;
        this.email = email;
        this.name = name;
        this.role = role;
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

    public String getRole() {
        return role;
    }
}
