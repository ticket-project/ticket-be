package com.ticket.core.domain.member;

import com.ticket.core.domain.member.vo.Email;
import com.ticket.core.domain.member.vo.MemberName;
import com.ticket.core.domain.member.vo.Password;
import com.ticket.core.enums.Role;

public class AddMember {

    private final Email email;
    private final Password password;
    private final MemberName name;
    private final Role role;

    public AddMember(final String email, final String rawPassword, final String name, final Role role) {
        this.email = new Email(email);
        this.password = new Password(rawPassword);
        this.name = new MemberName(name);
        this.role = role;
    }

    public String getEmailValue() {
        return email.getValue();
    }

    public Password getPassword() {
        return password;
    }

    public String getName() {
        return name.getValue();
    }

    public Role getRole() {
        return role;
    }
}
