package com.ticket.member;

public class PasswordPolicy {

    public static final int MIN_LENGTH = 4;

    public static void validate(final String password) {
        if (password == null) {
            throw new IllegalArgumentException("비밀번호는 null일 수 없습니다.");
        }
        if (password.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 공백일 수 없습니다.");
        }
        if (password.length() < MIN_LENGTH) {
            throw new IllegalArgumentException("비밀번호는 최소 " + MIN_LENGTH + "자 이상이어야 합니다.");
        }
    }
}
