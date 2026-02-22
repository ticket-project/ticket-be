package com.ticket.core.domain.auth.oauth2;

import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class KakaoUnlinkService {

    private static final String KAKAO_ADMIN_AUTH_PREFIX = "KakaoAK ";
    private static final String TARGET_ID_TYPE = "user_id";

    private final RestClient restClient;
    private final String adminKey;
    private final String unlinkUrl;

    public KakaoUnlinkService(
            final RestClient.Builder restClientBuilder,
            @Value("${app.auth.kakao.admin-key:}") final String adminKey,
            @Value("${app.auth.kakao.unlink-url:https://kapi.kakao.com/v1/user/unlink}") final String unlinkUrl
    ) {
        this.restClient = restClientBuilder.build();
        this.adminKey = adminKey;
        this.unlinkUrl = unlinkUrl;
    }

    public void unlinkByUserId(final String kakaoUserId) {
        if (!StringUtils.hasText(kakaoUserId)) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "카카오 사용자 ID가 비어 있습니다.");
        }

        if (!StringUtils.hasText(adminKey)) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "KAKAO_ADMIN_KEY 설정이 필요합니다.");
        }

        final MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("target_id_type", TARGET_ID_TYPE);
        formData.add("target_id", kakaoUserId);

        try {
            restClient.post()
                    .uri(unlinkUrl)
                    .header(HttpHeaders.AUTHORIZATION, KAKAO_ADMIN_AUTH_PREFIX + adminKey)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new CoreException(ErrorType.DEFAULT_ERROR, "카카오 unlink 호출에 실패했습니다.");
        }
    }
}
