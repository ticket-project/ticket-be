package com.ticket.core.support.exception;

import org.springframework.http.HttpStatus;

public enum ErrorType {
    DEFAULT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.E500, "알 수 없는 에러입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "요청하신 정보를 찾을 수 없습니다."),

    //회원
    DUPLICATE_EMAIL_ERROR(HttpStatus.CONFLICT, ErrorCode.E1000, "중복된 이메일은 불가능합니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, ErrorCode.E1001, "잘못된 비밀번호입니다."),

    //회차
    IS_NOT_VALID_PERFORMANCE(HttpStatus.BAD_REQUEST, ErrorCode.E2000, "유효하지 않은 공연 정보입니다."),
    IS_PAST_PERFORMANCE(HttpStatus.BAD_REQUEST, ErrorCode.E2001, "과거 공연은 예매할 수 없습니다."),
    NOT_YET_RESERVE_TIME(HttpStatus.BAD_REQUEST, ErrorCode.E2002, "아직 예매가 오픈되지 않았습니다."),
    NOT_EXIST_AVAILABLE_SEAT(HttpStatus.BAD_REQUEST, ErrorCode.E2003, "이용 가능한 좌석이 없습니다."),

    //좌석
    SEAT_MISMATCH_IN_PERFORMANCE(HttpStatus.BAD_REQUEST, ErrorCode.E3000, "요청한 좌석 정보와 일치하지 않습니다.");

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
