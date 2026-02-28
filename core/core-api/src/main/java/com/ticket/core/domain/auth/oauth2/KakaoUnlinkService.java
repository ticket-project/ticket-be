package com.ticket.core.domain.auth.oauth2;

import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class KakaoUnlinkService {

    private static final String KAKAO_ADMIN_AUTH_PREFIX = "KakaoAK ";
    private static final String TARGET_ID_TYPE = "user_id";

    private final KakaoApiClient kakaoApiClient;
    private final String adminKey;

    public KakaoUnlinkService(
            final KakaoApiClient kakaoApiClient,
            @Value("${app.auth.kakao.admin-key:}") final String adminKey
    ) {
        this.kakaoApiClient = kakaoApiClient;
        this.adminKey = adminKey;
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
            kakaoApiClient.unlink(KAKAO_ADMIN_AUTH_PREFIX + adminKey, formData);
        } catch (Exception e) {
            throw new CoreException(ErrorType.DEFAULT_ERROR, "카카오 unlink 호출에 실패했습니다.");
        }
    }
}
