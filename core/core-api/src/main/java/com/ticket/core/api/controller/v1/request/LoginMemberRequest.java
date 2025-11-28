package com.ticket.core.api.controller.v1.request;

import jakarta.validation.constraints.NotBlank;

public class LoginMemberRequest {

    @NotBlank
    private final String email;
    @NotBlank
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
