package com.ticket.core.domain.member.vo;

import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;

import java.util.Objects;

public class Password {
    private static final int MINIMUM_PASSWORD_LENGTH = 4;

    private final String value;

    private Password(final String value) {
        this.value = validateAndNormalize(value);
    }

    public static Password create(final String value) {
        return new Password(value);
    }

    private String validateAndNormalize(final String rawValue) {
        if (rawValue == null) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "password는 null일 수 없습니다.");
        }
        final String trimmedValue = rawValue.trim();
        if (trimmedValue.isEmpty()) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "password는 빈 값일 수 없습니다.");
        }
        if (trimmedValue.length() < MINIMUM_PASSWORD_LENGTH) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "password는 4자 이상이어야 합니다.");
        }
        return trimmedValue;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final Password password = (Password) o;
        return Objects.equals(value, password.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
