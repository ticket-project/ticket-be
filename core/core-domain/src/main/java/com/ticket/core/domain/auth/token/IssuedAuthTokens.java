package com.ticket.core.domain.auth.token;

public record IssuedAuthTokens(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        Long memberId
) {
}
