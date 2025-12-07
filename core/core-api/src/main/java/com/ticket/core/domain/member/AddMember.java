package com.ticket.core.domain.member;

import com.ticket.core.enums.Role;

public class AddMember {

    private final String email;
    private final String password;
    private final String name;
    private final Role role;

    public AddMember(final String email, final String password, final String name, final Role role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }
}
