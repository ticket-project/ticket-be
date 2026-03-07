package com.ticket.core.domain.auth;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * Redis 기반 Refresh Token 관리 서비스.
 * <p>
 * 저장 구조:
 * - Key: "refresh_token:{tokenValue}" → Value: memberId (String)
 * - TTL: refreshTokenExpirationSeconds
 * <p>
 * Token Rotation: refresh 시 기존 토큰 삭제 + 새 토큰 발급
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String KEY_PREFIX = "refresh_token:";
    private final RedissonClient redissonClient;

    /**
     * 새 Refresh Token을 생성하고 Redis에 저장합니다.
     */
    public String createRefreshToken(final Long memberId, final long expirationSeconds) {
        final String tokenValue = UUID.randomUUID().toString();
        final RBucket<String> bucket = redissonClient.getBucket(KEY_PREFIX + tokenValue);
        bucket.set(String.valueOf(memberId), Duration.ofSeconds(expirationSeconds));
        return tokenValue;
    }

    /**
     * Refresh Token을 검증하고 memberId를 반환합니다.
     */
    public Optional<Long> validate(final String tokenValue) {
        final RBucket<String> bucket = redissonClient.getBucket(KEY_PREFIX + tokenValue);
        final String memberId = bucket.get();
        if (memberId == null) {
            return Optional.empty();
        }
        return Optional.of(Long.parseLong(memberId));
    }

    /**
     * Refresh Token을 삭제합니다 (로그아웃 시 사용).
     */
    public void revoke(final String tokenValue) {
        redissonClient.getBucket(KEY_PREFIX + tokenValue).delete();
    }

    /**
     * Token Rotation: 기존 토큰 삭제 후 새 토큰 발급.
     */
    public String rotate(final String oldTokenValue, final Long memberId, final long expirationSeconds) {
        revoke(oldTokenValue);
        return createRefreshToken(memberId, expirationSeconds);
    }
}
