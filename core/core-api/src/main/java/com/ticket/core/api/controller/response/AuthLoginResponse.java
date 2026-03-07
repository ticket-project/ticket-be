package com.ticket.core.api.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답")
public record AuthLoginResponse(@Schema(description = "액세스 토큰(JWT)") String accessToken,
                                @Schema(description = "리프레시 토큰") String refreshToken,
                                @Schema(description = "토큰 타입", example = "Bearer") String tokenType,
                                @Schema(description = "액세스 토큰 만료 시간(초)", example = "1800") long expiresIn,
                                @Schema(description = "회원 ID", example = "1") Long memberId) {

    public AuthLoginResponse(
            final String accessToken,
            final String refreshToken,
            final String tokenType,
            final long expiresIn,
            final Long memberId
    ) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.memberId = memberId;
    }

}
