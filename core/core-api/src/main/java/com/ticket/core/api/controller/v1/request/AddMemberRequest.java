package com.ticket.core.api.controller.v1.request;

import com.ticket.core.domain.member.AddMember;
import com.ticket.core.enums.Role;
import jakarta.validation.constraints.Email;

public class AddMemberRequest {

    @Email
    private String email;
    private String password;
    private String name;
    private String role;

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

    public String getRole() {
        return role;
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
        if (Role.ADMIN.name().equals(role)) {
            return new AddMember(email, password, name, Role.ADMIN);
        }
        return new AddMember(email, password, name, Role.MEMBER);
    }
}
