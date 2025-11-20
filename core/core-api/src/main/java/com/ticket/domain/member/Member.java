package com.ticket.domain.member;

import com.ticket.domain.member.vo.Email;

public class Member {

    private final Long id;
    private final Email email;
    private final String name;
    private final String password;

    public Member(final Long id, final Email email, final String name, final String password) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.password = password;
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

    public String getPassword() {
        return password;
    }
}
