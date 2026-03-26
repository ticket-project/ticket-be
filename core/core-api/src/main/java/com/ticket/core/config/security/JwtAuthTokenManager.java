package com.ticket.core.config.security;

import com.ticket.core.domain.auth.token.AuthRefreshToken;
import com.ticket.core.domain.auth.token.AuthTokenManager;
import com.ticket.core.domain.auth.token.IssuedAuthTokens;
import com.ticket.core.domain.auth.token.RefreshTokenService;
import com.ticket.core.domain.member.model.Member;
import com.ticket.core.config.security.MemberPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtAuthTokenManager implements AuthTokenManager {

    private static final String TOKEN_TYPE_BEARER = "Bearer";

    private final JwtTokenService jwtTokenService;
    private final JwtProperties jwtProperties;
    private final RefreshTokenService refreshTokenService;

    @Override
    public IssuedAuthTokens issueTokens(final Member member) {
        final MemberPrincipal principal = new MemberPrincipal(member.getId(), member.getRole());
        final String accessToken = jwtTokenService.createAccessToken(principal);
        final String refreshToken = refreshTokenService.createRefreshToken(
                member.getId(),
                jwtProperties.getRefreshTokenExpirationSeconds()
        );

        return new IssuedAuthTokens(
                accessToken,
                refreshToken,
                TOKEN_TYPE_BEARER,
                jwtTokenService.getAccessTokenExpirationSeconds(),
                member.getId()
        );
    }

    @Override
    public IssuedAuthTokens rotateTokens(
            final Member member,
            final AuthRefreshToken refreshToken
    ) {
        final String newRefreshToken = refreshTokenService.rotate(
                refreshToken,
                member.getId(),
                jwtProperties.getRefreshTokenExpirationSeconds()
        );
        final MemberPrincipal principal = new MemberPrincipal(member.getId(), member.getRole());
        final String newAccessToken = jwtTokenService.createAccessToken(principal);

        return new IssuedAuthTokens(
                newAccessToken,
                newRefreshToken,
                TOKEN_TYPE_BEARER,
                jwtTokenService.getAccessTokenExpirationSeconds(),
                member.getId()
        );
    }
}
