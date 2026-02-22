package com.ticket.core.support.exception;

public enum ErrorCode {
    E400("잘못된 요청"),
    E404("데이터 없음"),
    E500("내부 서버 오류"),

    //AUTH
    E1000("인증 오류"),
    E1001("인가 오류"),

    //회원
    E2000("중복 이메일"),
    E2001("비밀번호 불일치"),

    //회차
    E3000("유효하지 않은 회차"),
    E3001("지난 회차"),
    E3002("예매 시작 전"),
    E3003("예매 가능한 좌석 없음"),

    //좌석
    E4000("회차 좌석 불일치"),

    //예매
    E5000("가용 좌석 초과"),
    E5001("좌석 수량 불일치"),

    //선점
    E6000("이미 선점된 좌석"),

    //공연
    E7000("미지원 공연 정렬"),
    ;

    private final String description;

    ErrorCode(final String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
