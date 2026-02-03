package com.ticket.core.domain.auth.oauth;

import com.ticket.core.enums.SocialProvider;

import java.util.Map;

public class GoogleOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public GoogleOAuth2UserInfo(final Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public SocialProvider getProvider() {
        return SocialProvider.GOOGLE;
    }

    @Override
    public String getProviderId() {
        return value("sub");
    }

    @Override
    public String getEmail() {
        return value("email");
    }

    @Override
    public String getName() {
        return value("name");
    }

    @Override
    public String getImageUrl() {
        return value("picture");
    }

    private String value(final String key) {
        final Object raw = attributes.get(key);
        return raw == null ? null : raw.toString();
    }
}
