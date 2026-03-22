package com.ticket.core.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class SocialLoginBaseUrlResolver {

    private static final String LOCALHOST = "localhost";
    private static final String LOOPBACK = "127.0.0.1";

    private final String localBaseUrl;
    private final String prodBaseUrl;
    private final String fallbackBaseUrl;

    public SocialLoginBaseUrlResolver(
            @Value("${app.auth.social-login.local-base-url}") final String localBaseUrl,
            @Value("${app.auth.social-login.prod-base-url}") final String prodBaseUrl,
            @Value("${app.auth.public-base-url}") final String fallbackBaseUrl
    ) {
        this.localBaseUrl = normalize(localBaseUrl);
        this.prodBaseUrl = normalize(prodBaseUrl);
        this.fallbackBaseUrl = normalize(fallbackBaseUrl);
    }

    public String resolve(
            final String origin,
            final String referer,
            final String requestBaseUrl
    ) {
        if (isLocalClient(origin) || isLocalClient(referer)) {
            return localBaseUrl;
        }
        if (hasClientHost(origin) || hasClientHost(referer)) {
            return prodBaseUrl;
        }
        if (hasText(requestBaseUrl)) {
            return normalize(requestBaseUrl);
        }
        return fallbackBaseUrl;
    }

    private boolean isLocalClient(final String uri) {
        final String host = extractHost(uri);
        return LOCALHOST.equals(host) || LOOPBACK.equals(host);
    }

    private boolean hasClientHost(final String uri) {
        return hasText(extractHost(uri));
    }

    private String extractHost(final String uri) {
        if (!hasText(uri)) {
            return null;
        }
        try {
            return URI.create(uri).getHost();
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private boolean hasText(final String value) {
        return value != null && !value.isBlank();
    }

    private String normalize(final String baseUrl) {
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }
}
