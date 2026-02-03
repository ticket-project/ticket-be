package com.ticket.core.api.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Token response")
public class TokenResponse {

    @Schema(description = "Access token")
    private final String accessToken;

    @Schema(description = "Refresh token")
    private final String refreshToken;

    @Schema(description = "Token type", example = "Bearer")
    private final String tokenType;

    @Schema(description = "Access token expiration (ms)")
    private final long expiresIn;

    public TokenResponse(
            final String accessToken,
            final String refreshToken,
            final String tokenType,
            final long expiresIn
    ) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }
}
