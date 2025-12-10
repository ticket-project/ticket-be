package com.ticket.core.domain.member.vo;

import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;

import java.util.Objects;
import java.util.regex.Pattern;

public class Email {

    private final String value;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private Email(final String value) {
        this.value = validate(value);
    }

    private static String validate(final String email) {
        if (email == null) {
            throw new CoreException(ErrorType.VALIDATION_ERROR, "email은 null일 수 없습니다.");
        }
        String trimmed = email.trim();
        if (trimmed.isEmpty()) {
            throw new CoreException(ErrorType.VALIDATION_ERROR, "email은 빈 값일 수 없습니다.");
        }
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new CoreException(ErrorType.VALIDATION_ERROR, "올바르지 않은 email 형식입니다.");
        }
        return trimmed;
    }

    public static Email create(final String value) {
        return new Email(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final Email email = (Email) o;
        return Objects.equals(this.value, email.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
