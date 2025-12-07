package com.ticket.core.domain.member;

public class LoginMember {
    private final Long memberId;

    public LoginMember(Long memberId) {
        this.memberId = memberId;
    }

    public Long getMemberId() {
        return memberId;
    }
}
