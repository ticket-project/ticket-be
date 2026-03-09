package com.ticket.core.domain.member.vo;

import lombok.Getter;

import java.util.Objects;
import java.util.regex.Pattern;

@Getter
public class RawPassword {
    private static final int MINIMUM_PASSWORD_LENGTH = 8;
    private static final int MAXIMUM_PASSWORD_LENGTH = 128;
    private static final Pattern HAS_LETTER = Pattern.compile("[a-zA-Z]");
    private static final Pattern HAS_DIGIT = Pattern.compile("\\d");
    private static final Pattern HAS_SPECIAL = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?`~]");

    private final String password;

    private RawPassword(final String password) {
        this.password = password;
    }

    public static RawPassword create(final String value) {
        return new RawPassword(value);
    }

//    private String validateAndNormalize(final String rawValue) {
//        if (rawValue == null) {
//            throw new CoreException(ErrorType.INVALID_REQUEST, "password는 null일 수 없습니다.");
//        }
//        final String trimmedValue = rawValue.trim();
//        if (trimmedValue.isEmpty()) {
//            throw new CoreException(ErrorType.INVALID_REQUEST, "password는 빈 값일 수 없습니다.");
//        }
//        if (trimmedValue.length() < MINIMUM_PASSWORD_LENGTH) {
//            throw new CoreException(ErrorType.INVALID_REQUEST,
//                    "password는 " + MINIMUM_PASSWORD_LENGTH + "자 이상이어야 합니다.");
//        }
//        if (trimmedValue.length() > MAXIMUM_PASSWORD_LENGTH) {
//            throw new CoreException(ErrorType.INVALID_REQUEST,
//                    "password는 " + MAXIMUM_PASSWORD_LENGTH + "자 이하여야 합니다.");
//        }
//        if (!HAS_LETTER.matcher(trimmedValue).find()) {
//            throw new CoreException(ErrorType.INVALID_REQUEST, "password는 영문자를 포함해야 합니다.");
//        }
//        if (!HAS_DIGIT.matcher(trimmedValue).find()) {
//            throw new CoreException(ErrorType.INVALID_REQUEST, "password는 숫자를 포함해야 합니다.");
//        }
//        if (!HAS_SPECIAL.matcher(trimmedValue).find()) {
//            throw new CoreException(ErrorType.INVALID_REQUEST, "password는 특수문자(!@#$%^&* 등)를 포함해야 합니다.");
//        }
//        return trimmedValue;
//    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final RawPassword rawPassword = (RawPassword) o;
        return Objects.equals(this.password, rawPassword.password);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(password);
    }
}
