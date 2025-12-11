package com.ticket.core.domain.member;

import com.ticket.core.domain.member.vo.Password;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordService {
    private final PasswordEncoder passwordEncoder;

    public PasswordService(final PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public String encode(final Password password) {
        return passwordEncoder.encode(password.getValue());
    }

    public boolean matches(final String rawPassword, final String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
