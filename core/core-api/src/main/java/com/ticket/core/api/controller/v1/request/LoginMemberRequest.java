package com.ticket.core.api.controller.v1.request;

public class LoginMemberRequest {
    private final String email;
    private final String password;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public LoginMemberRequest(final String email, final String password) {
        this.email = email;
        this.password = password;
    }
}
