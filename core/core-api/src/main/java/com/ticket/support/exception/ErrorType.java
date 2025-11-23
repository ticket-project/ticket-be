package com.ticket.support.exception;

import org.springframework.http.HttpStatus;

public enum ErrorType {
    DEFAULT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.E500, "알 수 없는 에러입니다."),

    //회원
    DUPLICATE_EMAIL_ERROR(HttpStatus.CONFLICT, ErrorCode.E409, "중복된 이메일은 불가능합니다.");

    private final HttpStatus status;
    private final ErrorCode errorCode;
    private final String message;

    ErrorType(final HttpStatus status, final ErrorCode errorCode, final String message) {
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
