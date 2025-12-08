package com.ticket.core.domain.member;

import com.ticket.core.enums.Role;

public class MemberDetails {
    private final Long memberId;
    private final Role role;

    public MemberDetails(final Long memberId, final Role role) {
        this.memberId = memberId;
        this.role = role;
    }

    public Long getMemberId() {
        return memberId;
    }

    public Role getRole() {
        return role;
    }
}
