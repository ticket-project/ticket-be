package com.ticket.core.domain.auth.infra.oauth2;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

import com.ticket.core.support.random.UuidSupplier;

/**
 * OAuth2 인증 이후 토큰을 URL에 직접 노출하지 않기 위한
 * 1회용 Authorization Code 관리 서비스.
 * <p>
 * 흐름:
 * 1. OAuth2 성공 → 임시 코드 생성 (Redis 30초 TTL)
 * 2. 프론트엔드 redirect → code 파라미터 수신
 * 3. POST /auth/oauth2/token {code} → 실제 토큰 교환
 */
@Service
@RequiredArgsConstructor
public class OAuth2AuthCodeService {

    private static final String KEY_PREFIX = "oauth2_auth_code:";
    private static final Duration CODE_TTL = Duration.ofSeconds(30);

    private final RedissonClient redissonClient;
    private final UuidSupplier uuidSupplier;

    /**
     * 1회용 인증 코드를 생성하고 Redis에 memberId를 저장합니다.
     */
    public String createCode(final Long memberId) {
        final String code = uuidSupplier.get().toString();
        final RBucket<String> bucket = redissonClient.getBucket(KEY_PREFIX + code);
        bucket.set(String.valueOf(memberId), CODE_TTL);
        return code;
    }

    /**
     * 인증 코드를 검증하고 memberId를 반환합니다.
     * 코드는 1회 사용 후 즉시 삭제됩니다.
     */
    public Optional<Long> consumeCode(final String code) {
        final RBucket<String> bucket = redissonClient.getBucket(KEY_PREFIX + code);
        final String memberId = bucket.getAndDelete();
        if (memberId == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(Long.parseLong(memberId));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
