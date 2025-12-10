package com.ticket.core.domain.member;

import com.ticket.core.domain.member.vo.Email;
import com.ticket.core.domain.member.vo.Password;
import com.ticket.core.enums.Role;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;

public class AddMember {

    private final Email email;
    private final Password password;
    private final String name;
    private final Role role;

    public AddMember(final String email, final String rawPassword, final String name, final Role role) {
        if (name == null || name.trim().isBlank()) {
            throw new CoreException(ErrorType.VALIDATION_ERROR, "이름은 공백일 수 없습니다.");
        }
        this.email = new Email(email);
        this.password = new Password(rawPassword);
        this.name = name;
        this.role = role;
    }

    public String getEmailValue() {
        return email.getValue();
    }

    public Password getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }
}
