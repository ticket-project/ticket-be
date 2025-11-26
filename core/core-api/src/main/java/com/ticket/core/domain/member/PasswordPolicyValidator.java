package com.ticket.core.domain.member;

import org.springframework.stereotype.Component;

@Component
public class PasswordPolicyValidator {

    private static final int MINIMUM_PASSWORD_LENGTH = 4;

    public void validateAdd(final String password) {
        if (password == null) {
            throw new IllegalArgumentException("비밀번호는 null일 수 없습니다.");
        }
        if (password.trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 공백일 수 없습니다.");
        }
        if (password.length() < MINIMUM_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("비밀번호는 최소 " + MINIMUM_PASSWORD_LENGTH + "자 이상이어야 합니다.");
        }
    }
}
