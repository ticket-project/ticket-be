package com.ticket.core.api.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답")
public class AuthLoginResponse {

    @Schema(description = "액세스 토큰(JWT)")
    private final String accessToken;

    @Schema(description = "토큰 타입", example = "Bearer")
    private final String tokenType;

    @Schema(description = "토큰 만료 시간(초)", example = "3600")
    private final long expiresIn;

    @Schema(description = "회원 ID", example = "1")
    private final Long memberId;

    public AuthLoginResponse(
            final String accessToken,
            final String tokenType,
            final long expiresIn,
            final Long memberId
    ) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.memberId = memberId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public Long getMemberId() {
        return memberId;
    }
}
