package com.ticket.core.domain.member;

import com.ticket.core.domain.member.vo.Email;

public class Member {

    private final Long id;
    private final Email email;
    private final String name;

    public Member(final Long id, final Email email, final String name) {
        this.id = id;
        this.email = email;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public Email getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

}
