package com.ticket.core.domain.hold.support;

import com.ticket.core.domain.hold.model.HoldSnapshot;
import com.ticket.core.domain.performanceseat.support.SeatRedisKey;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class HoldRedisService {

    private static final long LOCK_WAIT_SECONDS = 3L;
    private static final long LOCK_LEASE_SECONDS = 10L;
    private static final int RELEASE_LOCK_MAX_RETRIES = 3;
    private static final long RELEASE_LOCK_BACKOFF_MILLIS = 200L;
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
                log.warn("hold create lock acquire failed: performanceId={}, seatIds={}", performanceId, normalizedSeatIds);
                throw new CoreException(ErrorType.HOLD_BUSY);
            }
            ensureSeatsNotHeld(performanceId, normalizedSeatIds);
            saveHold(snapshot, ttl);
            return snapshot;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("hold create interrupted: performanceId={}, seatIds={}", performanceId, normalizedSeatIds, e);
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
            log.error("hold meta decode failed: holdToken={}", holdToken, e);
            return Optional.empty();
        }
    }

    public void releaseHold(final Long performanceId, final String holdToken, final List<Long> seatIds) {
        final List<Long> normalizedSeatIds = seatIds.stream().distinct().sorted().toList();
        final RLock multiLock = createSeatLock(performanceId, normalizedSeatIds);
        boolean locked = false;

        try {
            locked = tryAcquireReleaseLock(multiLock, performanceId, holdToken, normalizedSeatIds);
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
            throw new HoldReleaseLockException(
                    String.format("hold release interrupted: performanceId=%s, holdToken=%s", performanceId, holdToken),
                    e
            );
        } finally {
            if (locked) {
                unlockQuietly(multiLock);
            }
        }
    }

    public Set<Long> getHoldingSeatIds(final Long performanceId) {
        final Set<Long> seatIds = new HashSet<>();
        for (final String key : redissonClient.getKeys().getKeysByPattern(SeatRedisKey.holdPattern(performanceId))) {
            seatIds.add(SeatRedisKey.extractSeatId(key));
        }
        return seatIds;
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
                try {
                    redissonClient.getBucket(key, StringCodec.INSTANCE).delete();
                } catch (final Exception rollbackException) {
                    log.warn("hold rollback failed: key={}", key, rollbackException);
                }
            }
            try {
                redissonClient.getBucket(SeatRedisKey.holdMeta(snapshot.holdToken()), StringCodec.INSTANCE).delete();
            } catch (final Exception rollbackException) {
                log.warn("hold meta rollback failed: holdToken={}", snapshot.holdToken(), rollbackException);
            }
            throw new IllegalStateException("hold redis save failed", e);
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

    private boolean tryAcquireReleaseLock(
            final RLock multiLock,
            final Long performanceId,
            final String holdToken,
            final List<Long> seatIds
    ) throws InterruptedException {
        for (int attempt = 1; attempt <= RELEASE_LOCK_MAX_RETRIES; attempt++) {
            final boolean locked = multiLock.tryLock(LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
            if (locked) {
                if (attempt > 1) {
                    log.info("hold release lock acquired after retry: performanceId={}, holdToken={}, seatIds={}, attempt={}",
                            performanceId, holdToken, seatIds, attempt);
                }
                return true;
            }

            log.warn("hold release lock acquire failed: performanceId={}, holdToken={}, seatIds={}, attempt={}/{}",
                    performanceId, holdToken, seatIds, attempt, RELEASE_LOCK_MAX_RETRIES);

            if (attempt < RELEASE_LOCK_MAX_RETRIES) {
                Thread.sleep(RELEASE_LOCK_BACKOFF_MILLIS * (1L << (attempt - 1)));
            }
        }

        throw new HoldReleaseLockException(
                String.format("hold release lock acquire exhausted: performanceId=%s, holdToken=%s, seatIds=%s",
                        performanceId, holdToken, seatIds)
        );
    }

    private void unlockQuietly(final RLock lock) {
        try {
            lock.unlock();
        } catch (final IllegalMonitorStateException ignored) {
            log.debug("hold lock was not owned at unlock time");
        }
    }
}
