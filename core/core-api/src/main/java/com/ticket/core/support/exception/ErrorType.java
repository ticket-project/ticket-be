package com.ticket.core.support.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorType {
    DEFAULT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.E500, "알 수 없는 에러입니다."),
    NOT_FOUND_DATA(HttpStatus.NOT_FOUND, ErrorCode.E404, "요청하신 정보를 찾을 수 없습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, ErrorCode.E400, "요청이 올바르지 않습니다."),

    //AUTH
    AUTHENTICATION_ERROR(HttpStatus.UNAUTHORIZED, ErrorCode.E1000, "로그인이 필요합니다."),
    AUTHORIZATION_ERROR(HttpStatus.FORBIDDEN, ErrorCode.E1001, "권한이 없습니다."),

    //회원
    MEMBER_DUPLICATE_EMAIL(HttpStatus.CONFLICT, ErrorCode.E2000, "중복된 이메일은 불가능합니다."),
    MEMBER_NOT_MATCH_PASSWORD(HttpStatus.UNAUTHORIZED, ErrorCode.E2001, "비밀번호가 일치하지 않습니다."),

    //회차
    PERFORMANCE_IS_NOT_VALID(HttpStatus.BAD_REQUEST, ErrorCode.E3000, "유효하지 않은 공연 정보입니다."),
    PERFORMANCE_IS_PAST(HttpStatus.BAD_REQUEST, ErrorCode.E3001, "과거 공연은 예매할 수 없습니다."),
    NOT_YET_RESERVE_TIME(HttpStatus.BAD_REQUEST, ErrorCode.E3002, "아직 예매가 오픈되지 않았습니다."),
    NOT_EXIST_AVAILABLE_SEAT(HttpStatus.BAD_REQUEST, ErrorCode.E3003, "이용 가능한 좌석이 없습니다."),

    //좌석
    SEAT_MISMATCH_IN_PERFORMANCE(HttpStatus.BAD_REQUEST, ErrorCode.E4000, "요청한 좌석 정보와 일치하지 않습니다."),
    SEAT_ALREADY_SELECTED(HttpStatus.CONFLICT, ErrorCode.E4001, "이미 선택된 좌석입니다."),
    SEAT_NOT_OWNED(HttpStatus.FORBIDDEN, ErrorCode.E4002, "본인이 선택(Select)한 좌석이 아닙니다."),

    //예매
    EXCEED_AVAILABLE_SEATS(HttpStatus.CONFLICT, ErrorCode.E5000, "총 예매 가능 좌석을 초과하였습니다."),
    SEAT_COUNT_MISMATCH(HttpStatus.CONFLICT, ErrorCode.E5001, "요청한 좌석 중 일부가 예약 불가능합니다."),

    //선점
    SEAT_ALREADY_HOLD(HttpStatus.CONFLICT, ErrorCode.E6000, "좌석이 이미 선점되었습니다."),
    SEAT_HOLD_NOT_OWNED(HttpStatus.FORBIDDEN, ErrorCode.E6001, "본인이 선점한 좌석만 해제할 수 있습니다."),

    //공연
    NOT_SUPPORT_SHOW_SORT(HttpStatus.BAD_REQUEST, ErrorCode.E7000, "지원하지 않는 정렬 조건입니다."),
    SHOW_LIKE_ALREADY_EXISTS(HttpStatus.CONFLICT, ErrorCode.E7001, "이미 찜한 공연입니다."),

    //주문
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E8000, "주문을 찾을 수 없습니다."),
    ORDER_STATE_INVALID(HttpStatus.CONFLICT, ErrorCode.E8001, "현재 주문 상태에서는 해당 작업을 수행할 수 없습니다."),
    ORDER_NOT_OWNED(HttpStatus.FORBIDDEN, ErrorCode.E8002, "본인의 주문만 접근할 수 있습니다."),;

    private final HttpStatus status;
    private final ErrorCode errorCode;
    private final String message;

    ErrorType(final HttpStatus status, final ErrorCode errorCode, final String message) {
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
    }

    public String getDescription() {
        return message;
    }

}
