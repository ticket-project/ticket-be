package com.ticket.core.api.controller.v1.request;

import com.ticket.core.domain.member.AddMember;
import com.ticket.core.enums.Role;
import jakarta.validation.constraints.Email;

public class AddMemberRequest {

    @Email
    private String email;
    private String password;
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
        if (password.isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 공백일 수 없습니다.");
        }
        if (email.isEmpty()) {
            throw new IllegalArgumentException("이메일은 공백일 수 없습니다.");
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("이름은 공백일 수 없습니다.");
        }
        return new AddMember(email, password, name, Role.MEMBER);
    }
}
