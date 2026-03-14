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
            log.warn("좌석 선택에 실패했습니다. performanceId={}, seatId={}, memberId={}", performanceId, seatId, memberId);
            throw new CoreException(ErrorType.SEAT_ALREADY_SELECTED);
        }

        log.info("좌석 선택에 성공했습니다. performanceId={}, seatId={}, memberId={}", performanceId, seatId, memberId);
    }

    public void deselect(final Long performanceId, final Long seatId, final Long memberId) {
        final String key = SeatRedisKey.select(performanceId, seatId);
        final RBucket<String> bucket = redissonClient.getBucket(key, StringCodec.INSTANCE);
        final String memberKey = memberId.toString();

        final String holder = bucket.get();
        if (holder == null) {
            log.info("좌석 선택 해제를 건너뜁니다. 이미 선택 정보가 없습니다. performanceId={}, seatId={}", performanceId, seatId);
            return;
        }

        if (!memberKey.equals(holder)) {
            log.warn("좌석 선택 해제 권한이 없습니다. performanceId={}, seatId={}, requestMemberId={}, holderMemberId={}",
                    performanceId, seatId, memberId, holder);
            throw new CoreException(ErrorType.SEAT_NOT_OWNED);
        }

        final boolean released = bucket.compareAndSet(memberKey, null);
        if (!released) {
            final String currentHolder = bucket.get();
            if (currentHolder == null) {
                log.info("좌석 선택 해제 시점에 이미 만료되었거나 해제되었습니다. performanceId={}, seatId={}, memberId={}",
                        performanceId, seatId, memberId);
                return;
            }

            log.warn("좌석 선택 해제 권한이 없습니다. performanceId={}, seatId={}, requestMemberId={}, holderMemberId={}",
                    performanceId, seatId, memberId, currentHolder);
            throw new CoreException(ErrorType.SEAT_NOT_OWNED);
        }
        log.info("좌석 선택 해제에 성공했습니다. performanceId={}, seatId={}, memberId={}", performanceId, seatId, memberId);
    }

    public List<Long> deselectAll(final Long performanceId, final Long memberId) {
        final String pattern = SeatRedisKey.selectPattern(performanceId);
        final List<Long> deselectedSeatIds = new ArrayList<>();
        final String memberKey = memberId.toString();

        for (final String key : redissonClient.getKeys().getKeysByPattern(pattern)) {
            final RBucket<String> bucket = redissonClient.getBucket(key, StringCodec.INSTANCE);
            final String holder = bucket.get();
            if (!memberKey.equals(holder)) {
                continue;
            }

            if (!bucket.compareAndSet(memberKey, null)) {
                continue;
            }
            final Long seatId = SeatRedisKey.parseSelectKey(key).seatId();
            deselectedSeatIds.add(seatId);
            log.info("좌석 일괄 선택 해제에 성공했습니다. performanceId={}, seatId={}, memberId={}", performanceId, seatId, memberId);
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