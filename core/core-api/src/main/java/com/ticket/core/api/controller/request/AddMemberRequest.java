package com.ticket.core.api.controller.request;

import com.ticket.core.domain.member.AddMember;
import com.ticket.core.domain.member.vo.Email;
import com.ticket.core.domain.member.vo.RawPassword;
import com.ticket.core.enums.Role;
import jakarta.validation.constraints.NotBlank;

public class AddMemberRequest {

    @NotBlank
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
        return new AddMember(Email.create(email), RawPassword.create(password), name, Role.MEMBER);
    }
}
