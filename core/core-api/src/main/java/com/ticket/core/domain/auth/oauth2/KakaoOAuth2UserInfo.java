package com.ticket.core.domain.auth.oauth2;

import com.ticket.core.enums.SocialProvider;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class KakaoOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    @Override
    public SocialProvider provider() {
        return SocialProvider.KAKAO;
    }

    @Override
    public String providerId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String email() {
        final Map<String, Object> kakaoAccount = getMap(attributes, "kakao_account");
        return (String) kakaoAccount.get("email");
    }

    @Override
    public String name() {
        final Map<String, Object> kakaoAccount = getMap(attributes, "kakao_account");
        final Map<String, Object> profile = getMap(kakaoAccount, "profile");
        return (String) profile.get("nickname");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMap(final Map<String, Object> source, final String key) {
        final Object value = source.get(key);
        if (value instanceof Map<?, ?> mapValue) {
            return (Map<String, Object>) mapValue;
        }
        return Map.of();
    }
}
