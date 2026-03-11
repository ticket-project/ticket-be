package com.ticket.core.domain.auth;

import com.ticket.core.api.controller.response.AuthLoginResponse;
import com.ticket.core.config.security.JwtProperties;
import com.ticket.core.config.security.JwtTokenService;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.enums.Role;
import com.ticket.core.support.util.CookieUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthTokenApplicationService {

    private static final String TOKEN_TYPE_BEARER = "Bearer";

    private final JwtTokenService jwtTokenService;
    private final JwtProperties jwtProperties;
    private final RefreshTokenService refreshTokenService;

    public AuthLoginResponse issueTokens(final Member member, final HttpServletResponse response) {
        final MemberPrincipal principal = new MemberPrincipal(member.getId(), member.getRole());
        final String accessToken = jwtTokenService.createAccessToken(principal);
        final String refreshToken = refreshTokenService.createRefreshToken(
                member.getId(),
                jwtProperties.getRefreshTokenExpirationSeconds()
        );

        CookieUtils.addRefreshTokenCookie(response, refreshToken, jwtProperties.getRefreshTokenExpirationSeconds());

        return new AuthLoginResponse(
                accessToken,
                TOKEN_TYPE_BEARER,
                jwtTokenService.getAccessTokenExpirationSeconds(),
                member.getId()
        );
    }

    public AuthLoginResponse rotateTokens(
            final Member member,
            final String oldRefreshToken,
            final HttpServletResponse response
    ) {
        final String newRefreshToken = refreshTokenService.rotate(
                oldRefreshToken,
                member.getId(),
                jwtProperties.getRefreshTokenExpirationSeconds()
        );
        final MemberPrincipal principal = new MemberPrincipal(member.getId(), member.getRole());
        final String newAccessToken = jwtTokenService.createAccessToken(principal);

        CookieUtils.addRefreshTokenCookie(response, newRefreshToken, jwtProperties.getRefreshTokenExpirationSeconds());

        return new AuthLoginResponse(
                newAccessToken,
                TOKEN_TYPE_BEARER,
                jwtTokenService.getAccessTokenExpirationSeconds(),
                member.getId()
        );
    }

    public void clearRefreshTokenCookie(final HttpServletResponse response) {
        CookieUtils.deleteRefreshTokenCookie(response);
    }
}
