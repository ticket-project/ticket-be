package com.ticket.core.domain.member;

import com.ticket.core.domain.member.vo.Email;
import com.ticket.core.enums.Role;

public class Member {

    private final Long id;
    private final Email email;
    private final String name;
    private final Role role;

    public Member(final Long id, final Email email, final String name, final Role role) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getEmailValue() {
        return email.getValue();
    }

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }
}
