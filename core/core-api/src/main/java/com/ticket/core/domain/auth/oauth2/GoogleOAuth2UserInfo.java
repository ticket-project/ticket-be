package com.ticket.core.domain.auth.oauth2;

import com.ticket.core.enums.SocialProvider;

import java.util.Map;

public class GoogleOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public GoogleOAuth2UserInfo(final Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public SocialProvider provider() {
        return SocialProvider.GOOGLE;
    }

    @Override
    public String providerId() {
        return String.valueOf(attributes.get("sub"));
    }

    @Override
    public String email() {
        return (String) attributes.get("email");
    }

    @Override
    public String name() {
        return (String) attributes.get("name");
    }
}
