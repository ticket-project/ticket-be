package com.ticket.core.config.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    private static final String DEFAULT_ERROR_CODE = "oauth2_login_failed";
    private final String redirectUri;

    public OAuth2AuthenticationFailureHandler(
            @Value("${app.auth.oauth2-failure-redirect-uri}") final String redirectUri
    ) {
        this.redirectUri = redirectUri;
    }

    @Override
    public void onAuthenticationFailure(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final AuthenticationException exception
    ) throws IOException {
        log.warn("OAuth2 authentication failed: {}", exception.getMessage(), exception);
        final String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("error", DEFAULT_ERROR_CODE)
                .encode()
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
