package com.ticket.core.domain.performanceseat.command;

import com.ticket.core.domain.performanceseat.support.SeatRedisKey;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeatSelectionService {

    private static final Duration SELECT_TTL = Duration.ofMinutes(5);

    private final RedissonClient redissonClient;

    public void select(final Long performanceId, final Long seatId, final Long memberId) {
        final String key = SeatRedisKey.select(performanceId, seatId);
        final RBucket<String> bucket = redissonClient.getBucket(key, StringCodec.INSTANCE);

        final boolean locked = bucket.setIfAbsent(memberId.toString(), SELECT_TTL);
        if (!locked) {
            log.warn("seat select failed: performanceId={}, seatId={}, memberId={}", performanceId, seatId, memberId);
            throw new CoreException(ErrorType.SEAT_ALREADY_SELECTED);
        }

        log.info("seat select success: performanceId={}, seatId={}, memberId={}", performanceId, seatId, memberId);
    }

    public void deselect(final Long performanceId, final Long seatId, final Long memberId) {
        final String key = SeatRedisKey.select(performanceId, seatId);
        final RBucket<String> bucket = redissonClient.getBucket(key, StringCodec.INSTANCE);

        final String holder = bucket.get();
        if (holder == null) {
            log.info("seat deselect skipped: performanceId={}, seatId={}", performanceId, seatId);
            return;
        }

        if (!memberId.toString().equals(holder)) {
            log.warn("seat deselect denied: performanceId={}, seatId={}, requestMemberId={}, holderMemberId={}",
                    performanceId, seatId, memberId, holder);
            throw new CoreException(ErrorType.SEAT_NOT_OWNED);
        }

        bucket.delete();
        log.info("seat deselect success: performanceId={}, seatId={}, memberId={}", performanceId, seatId, memberId);
    }

    public List<Long> deselectAll(final Long performanceId, final Long memberId) {
        final String pattern = SeatRedisKey.selectPattern(performanceId);
        final List<Long> deselectedSeatIds = new ArrayList<>();

        for (final String key : redissonClient.getKeys().getKeysByPattern(pattern)) {
            final RBucket<String> bucket = redissonClient.getBucket(key, StringCodec.INSTANCE);
            final String holder = bucket.get();
            if (!memberId.toString().equals(holder)) {
                continue;
            }

            bucket.delete();
            final Long seatId = SeatRedisKey.parseSelectKey(key).seatId();
            deselectedSeatIds.add(seatId);
            log.info("seat bulk deselect success: performanceId={}, seatId={}, memberId={}", performanceId, seatId, memberId);
        }

        return deselectedSeatIds;
    }

    public void forceDeselect(final Long performanceId, final Long seatId) {
        redissonClient.getBucket(SeatRedisKey.select(performanceId, seatId), StringCodec.INSTANCE).delete();
    }

    public Set<Long> getSelectingSeatIds(final Long performanceId) {
        final String pattern = SeatRedisKey.selectPattern(performanceId);
        final Set<Long> seatIds = new HashSet<>();
        for (final String key : redissonClient.getKeys().getKeysByPattern(pattern)) {
            seatIds.add(SeatRedisKey.parseSelectKey(key).seatId());
        }
        return seatIds;
    }

}
