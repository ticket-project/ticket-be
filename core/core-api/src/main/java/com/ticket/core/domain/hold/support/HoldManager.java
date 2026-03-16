package com.ticket.core.domain.hold.support;

import com.ticket.core.aop.DistributedLock;
import com.ticket.core.domain.hold.application.HoldKeyGenerator;
import com.ticket.core.domain.hold.model.HoldSnapshot;
import com.ticket.core.domain.performanceseat.support.SeatRedisKey;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class HoldManager {

    private final RedissonClient redissonClient;
    private final HoldSnapshotCodec holdSnapshotCodec;
    private final HoldKeyGenerator holdKeyGenerator;
    private final Clock clock;

    @DistributedLock(
            prefix = "hold",
            dynamicKey = "#seatIds.![#performanceId + ':' + #this]"
    )
    public HoldSnapshot createHold(
            final Long memberId,
            final Long performanceId,
            final List<Long> seatIds,
            final Duration ttl
    ) {
        final String holdKey = holdKeyGenerator.generate();
        final LocalDateTime expiresAt = LocalDateTime.now(clock).plus(ttl);
        final HoldSnapshot snapshot = new HoldSnapshot(holdKey, memberId, performanceId, seatIds, expiresAt);

        ensureSeatsNotHeld(performanceId, seatIds);
        saveHold(snapshot, ttl);
        return snapshot;
    }

    @DistributedLock(
            prefix = "hold",
            dynamicKey = "#seatIds.![#performanceId + ':' + #this]"
    )
    public void release(final Long performanceId, final String holdKey, final List<Long> seatIds) {
        final List<Long> normalizedSeatIds = seatIds.stream().distinct().sorted().toList();
        for (final Long seatId : normalizedSeatIds) {
            final RBucket<String> bucket = redissonClient.getBucket(SeatRedisKey.hold(performanceId, seatId), StringCodec.INSTANCE);
            final String storedHoldKey = bucket.get();
            if (holdKey.equals(storedHoldKey)) {
                bucket.delete();
            }
        }
        redissonClient.getBucket(SeatRedisKey.holdMeta(holdKey), StringCodec.INSTANCE).delete();
    }

    public Set<Long> getHoldingSeatIds(final Long performanceId) {
        final Set<Long> seatIds = new HashSet<>();
        for (final String key : redissonClient.getKeys().getKeysByPattern(SeatRedisKey.holdPattern(performanceId))) {
            seatIds.add(SeatRedisKey.parseHoldKey(key).seatId());
        }
        return seatIds;
    }

    public boolean isHeld(final Long performanceId, final Long seatId) {
        final RBucket<String> bucket = redissonClient.getBucket(SeatRedisKey.hold(performanceId, seatId), StringCodec.INSTANCE);
        return bucket.get() != null;
    }

    private void ensureSeatsNotHeld(final Long performanceId, final List<Long> seatIds) {
        for (final Long seatId : seatIds) {
            if (isHeld(performanceId, seatId)) {
                throw new CoreException(ErrorType.SEAT_ALREADY_HOLD);
            }
        }
    }

    private void saveHold(final HoldSnapshot snapshot, final Duration ttl) {
        final List<String> createdKeys = new ArrayList<>();
        try {
            for (final Long seatId : snapshot.seatIds()) {
                final String key = SeatRedisKey.hold(snapshot.performanceId(), seatId);
                redissonClient.getBucket(key, StringCodec.INSTANCE).set(snapshot.holdKey(), ttl);
                createdKeys.add(key);
            }
            final String json = holdSnapshotCodec.encode(snapshot);
            redissonClient.getBucket(SeatRedisKey.holdMeta(snapshot.holdKey()), StringCodec.INSTANCE).set(json, ttl);
        } catch (final Exception e) {
            for (final String key : createdKeys) {
                try {
                    redissonClient.getBucket(key, StringCodec.INSTANCE).delete();
                } catch (final Exception rollbackException) {
                    log.warn("홀드 롤백에 실패했습니다. key={}", key, rollbackException);
                }
            }
            try {
                redissonClient.getBucket(SeatRedisKey.holdMeta(snapshot.holdKey()), StringCodec.INSTANCE).delete();
            } catch (final Exception rollbackException) {
                log.warn("홀드 메타 롤백에 실패했습니다. holdKey={}", snapshot.holdKey(), rollbackException);
            }
            throw new IllegalStateException("hold Redis 저장에 실패했습니다.", e);
        }
    }
}
