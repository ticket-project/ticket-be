package com.ticket.core.domain.auth.infra.token;

import com.ticket.core.domain.auth.token.AuthRefreshToken;
import com.ticket.core.support.random.UuidSupplier;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis 기반 Refresh Token 관리 서비스.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String KEY_PREFIX = "refresh_token:";
    private final RedissonClient redissonClient;
    private final UuidSupplier uuidSupplier;

    /**
     * 새 Refresh Token을 생성하고 Redis에 저장합니다.
     */
    public String createRefreshToken(final Long memberId, final long expirationSeconds) {
        final String tokenValue = uuidSupplier.get().toString();
        final RBucket<String> bucket = redissonClient.getBucket(KEY_PREFIX + tokenValue);
        bucket.set(String.valueOf(memberId), Duration.ofSeconds(expirationSeconds));
        return tokenValue;
    }

    /**
     * Refresh Token을 검증하고 원자적으로 소비합니다.
     */
    public Optional<Long> validate(final AuthRefreshToken refreshToken) {
        final String memberId = bucketOf(refreshToken).getAndDelete();
        return parseMemberId(memberId);
    }

    /**
     * Refresh Token을 소비하지 않고 소유자 memberId만 검증합니다.
     */
    public Optional<Long> validateWithoutConsume(final AuthRefreshToken refreshToken) {
        return parseMemberId(bucketOf(refreshToken).get());
    }

    /**
     * Refresh Token을 삭제합니다.
     */
    public void revoke(final AuthRefreshToken refreshToken) {
        bucketOf(refreshToken).delete();
    }

    /**
     * Refresh Token의 소유자가 일치할 때만 원자적으로 삭제합니다.
     */
    public boolean revokeIfOwned(final AuthRefreshToken refreshToken, final Long memberId) {
        return bucketOf(refreshToken).compareAndSet(String.valueOf(memberId), null);
    }

    /**
     * Token Rotation: 기존 토큰 제거 + 새 토큰 발급.
     */
    public String rotate(final AuthRefreshToken refreshToken, final Long memberId, final long expirationSeconds) {
        revoke(refreshToken);
        return createRefreshToken(memberId, expirationSeconds);
    }

    private RBucket<String> bucketOf(final AuthRefreshToken refreshToken) {
        return redissonClient.getBucket(KEY_PREFIX + refreshToken.value());
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
