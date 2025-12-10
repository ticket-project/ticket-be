package com.ticket.core.domain.member.vo;

import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;

public class Password {
    private static final int MINIMUM_PASSWORD_LENGTH = 4;

    private final String value;

    private Password(final String value) {
        validate(value);
        this.value = value;
    }

    public static Password create(final String value) {
        return new Password(value);
    }

    private void validate(final String rawValue) {
        if (rawValue == null) {
            throw new CoreException(ErrorType.VALIDATION_ERROR, "password는 null일 수 없습니다.");
        }
        if (rawValue.trim().isEmpty()) {
            throw new CoreException(ErrorType.VALIDATION_ERROR, "password는 빈 값일 수 없습니다.");
        }
        if (rawValue.length() < MINIMUM_PASSWORD_LENGTH) {
            throw new CoreException(ErrorType.VALIDATION_ERROR, "password는 4자 이상이어야 합니다.");
        }
    }

    public String getValue() {
        return value;
    }
}
