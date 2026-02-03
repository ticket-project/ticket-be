package com.ticket.core.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final OAuth2Properties oauth2Properties;

    public OAuth2LoginSuccessHandler(final JwtTokenProvider jwtTokenProvider, final OAuth2Properties oauth2Properties) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.oauth2Properties = oauth2Properties;
    }

    @Override
    public void onAuthenticationSuccess(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Authentication authentication
    ) throws IOException, ServletException {
        final CustomOidcUser principal = (CustomOidcUser) authentication.getPrincipal();
        final String accessToken = jwtTokenProvider.createAccessToken(principal.getMemberId(), principal.getRole());
        final String refreshToken = jwtTokenProvider.createRefreshToken(principal.getMemberId(), principal.getRole());

        final String targetUrl = UriComponentsBuilder.fromUriString(oauth2Properties.getRedirectUrl())
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .queryParam("tokenType", "Bearer")
                .queryParam("expiresIn", jwtTokenProvider.getAccessTokenExpiresIn())
                .build()
                .toUriString();

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
