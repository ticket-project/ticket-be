package com.ticket.core.domain.auth;

import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import org.springframework.stereotype.Component;

@Component
public class PasswordPolicyValidator {

    private static final int MINIMUM_PASSWORD_LENGTH = 4;

    public void validateAdd(final String password) {
        if (password == null) {
            throw new CoreException(ErrorType.INVALID_PASSWORD);
        }
        if (password.trim().isEmpty()) {
            throw new CoreException(ErrorType.INVALID_PASSWORD);
        }
        if (password.length() < MINIMUM_PASSWORD_LENGTH) {
            throw new CoreException(ErrorType.INVALID_PASSWORD);
        }
    }
}
