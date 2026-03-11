package com.ticket.core.domain.hold;

import com.ticket.core.domain.performanceseat.SeatRedisKey;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class HoldRedisService {

    private static final long LOCK_WAIT_SECONDS = 3L;
    private static final long LOCK_LEASE_SECONDS = 10L;
    private static final String HOLD_LOCK_PREFIX = "LOCK:HOLD:";

    private final RedissonClient redissonClient;
    private final HoldSnapshotCodec holdSnapshotCodec;

    public HoldSnapshot createHold(
            final Long memberId,
            final Long performanceId,
            final List<Long> seatIds,
            final Duration ttl
    ) {
        final List<Long> normalizedSeatIds = seatIds.stream().distinct().sorted().toList();
        final String holdToken = UUID.randomUUID().toString();
        final LocalDateTime expiresAt = LocalDateTime.now().plus(ttl);
        final HoldSnapshot snapshot = new HoldSnapshot(holdToken, memberId, performanceId, normalizedSeatIds, expiresAt);
        final RLock multiLock = createSeatLock(performanceId, normalizedSeatIds);

        try {
            final boolean locked = multiLock.tryLock(LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
            if (!locked) {
                throw new CoreException(ErrorType.HOLD_BUSY);
            }
            ensureSeatsNotHeld(performanceId, normalizedSeatIds);
            saveHold(snapshot, ttl);
            return snapshot;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CoreException(ErrorType.HOLD_PROCESSING_FAILED);
        } finally {
            unlockQuietly(multiLock);
        }
    }

    public Optional<HoldSnapshot> getHold(final String holdToken) {
        final RBucket<String> bucket = redissonClient.getBucket(SeatRedisKey.holdMeta(holdToken), StringCodec.INSTANCE);
        final String payload = bucket.get();
        if (payload == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(holdSnapshotCodec.decode(payload));
        } catch (final Exception e) {
            log.error("hold meta 역직렬화 실패: holdToken={}", holdToken, e);
            return Optional.empty();
        }
    }

    public void releaseHold(final String holdToken) {
        getHold(holdToken).ifPresent(snapshot -> releaseHold(snapshot.performanceId(), holdToken, snapshot.seatIds()));
    }

    public void releaseHold(final Long performanceId, final String holdToken, final List<Long> seatIds) {
        final List<Long> normalizedSeatIds = seatIds.stream().distinct().sorted().toList();
        final RLock multiLock = createSeatLock(performanceId, normalizedSeatIds);

        try {
            final boolean locked = multiLock.tryLock(LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
            if (!locked) {
                log.warn("hold 해제 락 획득 실패: performanceId={}, holdToken={}", performanceId, holdToken);
                return;
            }
            for (final Long seatId : normalizedSeatIds) {
                final RBucket<String> bucket = redissonClient.getBucket(SeatRedisKey.hold(performanceId, seatId), StringCodec.INSTANCE);
                final String storedToken = bucket.get();
                if (holdToken.equals(storedToken)) {
                    bucket.delete();
                }
            }
            redissonClient.getBucket(SeatRedisKey.holdMeta(holdToken), StringCodec.INSTANCE).delete();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("hold 해제 인터럽트: performanceId={}, holdToken={}", performanceId, holdToken, e);
        } finally {
            unlockQuietly(multiLock);
        }
    }

    public Set<Long> getHoldingSeatIds(final Long performanceId) {
        final Set<Long> seatIds = new HashSet<>();
        for (final String key : redissonClient.getKeys().getKeysByPattern(SeatRedisKey.holdPattern(performanceId))) {
            seatIds.add(SeatRedisKey.extractSeatId(key));
        }
        return seatIds;
    }

    private void saveHold(final HoldSnapshot snapshot, final Duration ttl) {
        final List<String> createdKeys = new ArrayList<>();
        try {
            for (final Long seatId : snapshot.seatIds()) {
                final String key = SeatRedisKey.hold(snapshot.performanceId(), seatId);
                redissonClient.getBucket(key, StringCodec.INSTANCE).set(snapshot.holdToken(), ttl);
                createdKeys.add(key);
            }
            final String json = holdSnapshotCodec.encode(snapshot);
            redissonClient.getBucket(SeatRedisKey.holdMeta(snapshot.holdToken()), StringCodec.INSTANCE).set(json, ttl);
        } catch (final Exception e) {
            for (final String key : createdKeys) {
                redissonClient.getBucket(key, StringCodec.INSTANCE).delete();
            }
            redissonClient.getBucket(SeatRedisKey.holdMeta(snapshot.holdToken()), StringCodec.INSTANCE).delete();
            throw new IllegalStateException("hold redis 저장 실패", e);
        }
    }

    private void ensureSeatsNotHeld(final Long performanceId, final List<Long> seatIds) {
        for (final Long seatId : seatIds) {
            final RBucket<String> bucket = redissonClient.getBucket(SeatRedisKey.hold(performanceId, seatId), StringCodec.INSTANCE);
            final String holdToken = bucket.get();
            if (holdToken != null) {
                throw new CoreException(ErrorType.SEAT_ALREADY_HOLD);
            }
        }
    }

    private RLock createSeatLock(final Long performanceId, final List<Long> seatIds) {
        if (seatIds.size() == 1) {
            return redissonClient.getLock(lockKey(performanceId, seatIds.getFirst()));
        }
        final RLock[] locks = seatIds.stream()
                .map(seatId -> redissonClient.getLock(lockKey(performanceId, seatId)))
                .toArray(RLock[]::new);
        return redissonClient.getMultiLock(locks);
    }

    private String lockKey(final Long performanceId, final Long seatId) {
        return HOLD_LOCK_PREFIX + performanceId + ":" + seatId;
    }

    private void unlockQuietly(final RLock lock) {
        try {
            lock.unlock();
        } catch (final IllegalMonitorStateException ignored) {
            log.debug("hold 락이 이미 해제되었습니다.");
        }
    }

}
