package com.ticket.core.domain.auth;

import com.ticket.core.domain.member.vo.EncodedPassword;
import com.ticket.core.domain.member.vo.RawPassword;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordService {
    private final PasswordEncoder passwordEncoder;

    public String encode(final String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public boolean matches(final RawPassword rawPassword, final EncodedPassword encodedPassword) {
        return passwordEncoder.matches(rawPassword.getPassword(), encodedPassword.getPassword());
    }
}
