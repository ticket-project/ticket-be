package com.ticket.core.domain.member.vo;

import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import java.util.Objects;

public class MemberName {

    private final String value;

    public MemberName(final String value) {
        if (value == null || value.isBlank()) {
            throw new CoreException(ErrorType.VALIDATION_ERROR, "이름은 공백일 수 없습니다.");
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final MemberName that = (MemberName) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
