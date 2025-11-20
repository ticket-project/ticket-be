package com.ticket.domain.member;

public class AddMember {

    private final String email;
    private final String password;
    private final String name;

    public AddMember(final String email, final String password, final String name) {
        this.email = email;
        this.password = password;
        this.name = name;
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
}
