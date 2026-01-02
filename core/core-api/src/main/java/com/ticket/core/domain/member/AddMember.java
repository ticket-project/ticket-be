package com.ticket.core.domain.member;

import com.ticket.core.domain.member.vo.Email;
import com.ticket.core.domain.member.vo.RawPassword;
import com.ticket.core.enums.Role;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;

public class AddMember {

    private final Email email;
    private final RawPassword rawPassword;
    private final String name;
    private final Role role;

    public AddMember(final Email email, final RawPassword rawPassword, final String name, final Role role) {
        if (name == null || name.trim().isBlank()) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "이름은 공백일 수 없습니다.");
        }
        this.email = email;
        this.rawPassword = rawPassword;
        this.name = name;
        this.role = role;
    }

    public Email getEmail() {
        return email;
    }

    public RawPassword getRawPassword() {
        return rawPassword;
    }

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }
}
