package com.ticket.core.domain.performanceseat;

import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Redis 기반 좌석 선점(Hold) 관리 서비스.
 * Lua 스크립트를 사용하여 다건 좌석을 원자적으로 Hold합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeatHoldService {

    private final RedissonClient redissonClient;

    private static final String HOLD_LUA_SCRIPT = """
            local holdTime = tonumber(ARGV[1])
            local memberId = ARGV[2]
            
            for i, key in ipairs(KEYS) do
                if redis.call('EXISTS', key) == 1 then
                    return 0
                end
            end
            
            for i, key in ipairs(KEYS) do
                redis.call('SET', key, memberId, 'EX', holdTime)
            end
            
            return 1
            """;

    /**
     * 다건 좌석을 원자적으로 선점합니다.
     * 하나라도 이미 Hold된 좌석이 있으면 전체 실패합니다.
     */
    public void hold(final Long performanceId, final List<Long> seatIds, final Long memberId, final int holdTimeSec) {
        final List<Object> keys = seatIds.stream()
                .map(seatId -> (Object) SeatRedisKey.hold(performanceId, seatId))
                .toList();

        final Long result = redissonClient.getScript(StringCodec.INSTANCE)
                .eval(RScript.Mode.READ_WRITE, HOLD_LUA_SCRIPT, RScript.ReturnType.VALUE,
                        keys, String.valueOf(holdTimeSec), memberId.toString());

        if (result == null || result == 0L) {
            log.warn("좌석 선점 실패 (이미 선점됨): performanceId={}, seatIds={}, memberId={}",
                    performanceId, seatIds, memberId);
            throw new CoreException(ErrorType.SEAT_ALREADY_HOLD);
        }

        log.info("좌석 선점 성공: performanceId={}, seatIds={}, memberId={}, holdTime={}s",
                performanceId, seatIds, memberId, holdTimeSec);
    }

    /**
     * 선점을 해제합니다. 본인이 Hold한 좌석만 해제 가능합니다.
     */
    public void release(final Long performanceId, final List<Long> seatIds, final Long memberId) {
        for (final Long seatId : seatIds) {
            final String key = SeatRedisKey.hold(performanceId, seatId);
            final RBucket<String> bucket = redissonClient.getBucket(key, StringCodec.INSTANCE);

            final String holder = bucket.get();
            if (holder == null) {
                log.info("선점 해제 무시 (이미 해제됨): performanceId={}, seatId={}", performanceId, seatId);
                continue;
            }

            if (!memberId.toString().equals(holder)) {
                log.warn("선점 해제 권한 없음: performanceId={}, seatId={}, requestMemberId={}, holderMemberId={}",
                        performanceId, seatId, memberId, holder);
                throw new CoreException(ErrorType.SEAT_HOLD_NOT_OWNED);
            }

            bucket.delete();
            log.info("선점 해제 성공: performanceId={}, seatId={}, memberId={}", performanceId, seatId, memberId);
        }
    }

    /**
     * 특정 공연의 Hold 상태 좌석 ID 목록을 조회합니다.
     */
    public Set<Long> getHeldSeatIds(final Long performanceId) {
        final String pattern = SeatRedisKey.holdPattern(performanceId);
        final Set<Long> seatIds = new HashSet<>();
        for (final String key : redissonClient.getKeys().getKeysByPattern(pattern)) {
            seatIds.add(SeatRedisKey.extractSeatId(key));
        }
        return seatIds;
    }

    /**
     * Hold 소유자의 memberId를 반환합니다.
     */
    public String getHolderMemberId(final Long performanceId, final Long seatId) {
        final String key = SeatRedisKey.hold(performanceId, seatId);
        return redissonClient.<String>getBucket(key, StringCodec.INSTANCE).get();
    }
}
