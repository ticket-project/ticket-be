package com.ticket.core.domain.member;

public class MemberDetails {
    private final Long memberId;

    public MemberDetails(Long memberId) {
        this.memberId = memberId;
    }

    public Long getMemberId() {
        return memberId;
    }
}
