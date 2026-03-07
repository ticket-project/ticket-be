package com.ticket.core.domain.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 인증 이벤트 로거.
 * 로그인 성공/실패, 로그아웃, 토큰 갱신, OAuth2 로그인 등
 * 인증 관련 이벤트를 구조화된 로그로 기록합니다.
 * <p>
 * 로그 형식: [AUTH_EVENT] event=이벤트명 | memberId=회원ID | detail=상세정보
 */
@Slf4j
@Component
public class AuthEventLogger {

    private static final String LOG_FORMAT = "[AUTH_EVENT] event={} | memberId={} | detail={}";

    public void loginSuccess(final Long memberId) {
        log.info(LOG_FORMAT, "LOGIN_SUCCESS", memberId, "일반 로그인 성공");
    }

    public void loginFail(final String email, final String reason) {
        log.warn(LOG_FORMAT, "LOGIN_FAIL", email, reason);
    }

    public void logout(final Long memberId) {
        log.info(LOG_FORMAT, "LOGOUT", memberId, "로그아웃");
    }

    public void tokenRefresh(final Long memberId) {
        log.info(LOG_FORMAT, "TOKEN_REFRESH", memberId, "토큰 갱신");
    }

    public void oauth2LoginSuccess(final Long memberId, final String provider) {
        log.info(LOG_FORMAT, "OAUTH2_LOGIN_SUCCESS", memberId, "소셜 로그인 성공 (" + provider + ")");
    }

    public void signUp(final Long memberId) {
        log.info(LOG_FORMAT, "SIGN_UP", memberId, "회원가입");
    }
}
