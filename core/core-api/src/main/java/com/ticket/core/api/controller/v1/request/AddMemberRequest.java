package com.ticket.core.api.controller.v1.request;

import com.ticket.core.domain.member.AddMember;
import com.ticket.core.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AddMemberRequest {

    @Email
    private String email;
    @NotBlank
    private String password;
    @NotBlank
    private String name;

    public AddMemberRequest() {}

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public AddMember toAddMember() {
        return new AddMember(email, password, name, Role.MEMBER);
    }
}
