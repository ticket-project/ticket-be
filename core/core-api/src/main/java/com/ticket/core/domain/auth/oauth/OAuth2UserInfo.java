package com.ticket.core.domain.auth.oauth;

import com.ticket.core.enums.SocialProvider;

public interface OAuth2UserInfo {
    SocialProvider getProvider();

    String getProviderId();

    String getEmail();

    String getName();

    String getImageUrl();
}
