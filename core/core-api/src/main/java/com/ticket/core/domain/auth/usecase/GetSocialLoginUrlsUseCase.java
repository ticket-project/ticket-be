package com.ticket.core.domain.auth.usecase;

import com.ticket.core.config.security.OAuth2EndpointConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GetSocialLoginUrlsUseCase {

    private static final String GOOGLE_REGISTRATION_ID = "google";
    private static final String KAKAO_REGISTRATION_ID = "kakao";

    public record Input(String baseUrl) {}
    public record Output(Map<String, String> urls) {}

    public Output execute(final Input input) {
        return new Output(Map.of(
                GOOGLE_REGISTRATION_ID, buildSocialLoginUrl(input.baseUrl(), GOOGLE_REGISTRATION_ID),
                KAKAO_REGISTRATION_ID, buildSocialLoginUrl(input.baseUrl(), KAKAO_REGISTRATION_ID)
        ));
    }

    private String buildSocialLoginUrl(final String baseUrl, final String registrationId) {
        return baseUrl + OAuth2EndpointConstants.AUTHORIZATION_BASE_URI + "/" + registrationId;
    }
}
