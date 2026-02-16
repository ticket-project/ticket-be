package com.ticket.core.config.security;

import com.ticket.core.domain.member.MemberPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenService jwtTokenService;
    private final String redirectUri;

    public OAuth2AuthenticationSuccessHandler(
            final JwtTokenService jwtTokenService,
            @Value("${app.auth.oauth2-success-redirect-uri}") final String redirectUri
    ) {
        this.jwtTokenService = jwtTokenService;
        this.redirectUri = redirectUri;
    }

    @Override
    public void onAuthenticationSuccess(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Authentication authentication
    ) throws IOException, ServletException {
        if (!(authentication.getPrincipal() instanceof MemberPrincipal memberPrincipal)) {
            throw new IllegalStateException("Unsupported principal type: " + authentication.getPrincipal().getClass().getName());
        }
        final String accessToken = jwtTokenService.createAccessToken(memberPrincipal);

        final String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("accessToken", accessToken)
                .queryParam("tokenType", "Bearer")
                .queryParam("expiresIn", jwtTokenService.getAccessTokenExpirationSeconds())
                .queryParam("memberId", memberPrincipal.getMemberId())
                .build(true)
                .toUriString();

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
