package com.ticket.core.domain.auth.oauth;

import com.ticket.core.enums.SocialProvider;
import com.ticket.core.support.exception.AuthException;
import com.ticket.core.support.exception.ErrorType;

import java.util.Map;

public final class OAuth2UserInfoFactory {

    private OAuth2UserInfoFactory() {
    }

    public static OAuth2UserInfo create(final SocialProvider provider, final Map<String, Object> attributes) {
        if (provider == SocialProvider.GOOGLE) {
            return new GoogleOAuth2UserInfo(attributes);
        }
        throw new AuthException(ErrorType.INVALID_REQUEST, "Unsupported provider: " + provider);
    }
}
