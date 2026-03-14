package com.ticket.core.domain.auth.token;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * Redis 기반 Refresh Token 관리 서비스.
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
     * Refresh Token을 검증하고 원자적으로 소비합니다.
     */
    public Optional<Long> validate(final String tokenValue) {
        final RBucket<String> bucket = redissonClient.getBucket(KEY_PREFIX + tokenValue);
        final String memberId = bucket.getAndDelete();
        return parseMemberId(memberId);
    }

    /**
     * Refresh Token을 소비하지 않고 소유자 memberId만 검증합니다.
     */
    public Optional<Long> validateWithoutConsume(final String tokenValue) {
        final RBucket<String> bucket = redissonClient.getBucket(KEY_PREFIX + tokenValue);
        return parseMemberId(bucket.get());
    }

    /**
     * Refresh Token을 삭제합니다.
     */
    public void revoke(final String tokenValue) {
        redissonClient.getBucket(KEY_PREFIX + tokenValue).delete();
    }

    /**
     * Refresh Token의 소유자가 일치할 때만 원자적으로 삭제합니다.
     */
    public boolean revokeIfOwned(final String tokenValue, final Long memberId) {
        final RBucket<String> bucket = redissonClient.getBucket(KEY_PREFIX + tokenValue);
        return bucket.compareAndSet(String.valueOf(memberId), null);
    }

    /**
     * Token Rotation: 기존 토큰 제거 + 새 토큰 발급.
     */
    public String rotate(final String oldTokenValue, final Long memberId, final long expirationSeconds) {
        revoke(oldTokenValue);
        return createRefreshToken(memberId, expirationSeconds);
    }

    private Optional<Long> parseMemberId(final String memberId) {
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
