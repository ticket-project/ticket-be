package com.ticket.core.domain.member;

import com.ticket.core.enums.Role;

public class Member {

    private final Long id;
    private final String email;
    private final String name;
    private final Role role;

    public Member(final Long id, final String email, final String name, final Role role) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }
}
