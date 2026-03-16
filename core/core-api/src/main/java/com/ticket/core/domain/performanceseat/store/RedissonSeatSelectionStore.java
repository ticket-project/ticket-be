package com.ticket.core.domain.performanceseat.store;

import com.ticket.core.domain.performanceseat.support.SeatRedisKey;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RedissonSeatSelectionStore implements SeatSelectionStore {

    private final RedissonClient redissonClient;

    @Override
    public boolean selectIfAbsent(final Long performanceId, final Long seatId, final String memberId, final Duration ttl) {
        return bucket(performanceId, seatId).setIfAbsent(memberId, ttl);
    }

    @Override
    public String getHolder(final Long performanceId, final Long seatId) {
        return bucket(performanceId, seatId).get();
    }

    @Override
    public boolean releaseIfOwned(final Long performanceId, final Long seatId, final String memberId) {
        return bucket(performanceId, seatId).compareAndSet(memberId, null);
    }

    @Override
    public List<Long> releaseAllByMember(final Long performanceId, final String memberId) {
        final List<Long> deselectedSeatIds = new ArrayList<>();
        for (final String key : redissonClient.getKeys().getKeysByPattern(SeatRedisKey.selectPattern(performanceId))) {
            final RBucket<String> bucket = redissonClient.getBucket(key, StringCodec.INSTANCE);
            if (!memberId.equals(bucket.get())) {
                continue;
            }
            if (!bucket.compareAndSet(memberId, null)) {
                continue;
            }
            deselectedSeatIds.add(SeatRedisKey.parseSelectKey(key).seatId());
        }
        return deselectedSeatIds;
    }

    @Override
    public void forceRelease(final Long performanceId, final Long seatId) {
        bucket(performanceId, seatId).delete();
    }

    @Override
    public Set<Long> getSelectingSeatIds(final Long performanceId) {
        final Set<Long> seatIds = new HashSet<>();
        for (final String key : redissonClient.getKeys().getKeysByPattern(SeatRedisKey.selectPattern(performanceId))) {
            seatIds.add(SeatRedisKey.parseSelectKey(key).seatId());
        }
        return seatIds;
    }

    private RBucket<String> bucket(final Long performanceId, final Long seatId) {
        return redissonClient.getBucket(SeatRedisKey.select(performanceId, seatId), StringCodec.INSTANCE);
    }
}
