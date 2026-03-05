package com.ticket.core.domain.performanceseat;

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

/**
 * Redis 기반 좌석 선택 상태 관리 서비스.
 * 순수 인프라 레이어 역할로, Redis에 좌석 선택 상태를 저장/조회/삭제합니다.
 * WebSocket 브로드캐스트는 UseCase 레이어에서 처리합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeatSelectionService {

    private static final Duration SELECT_TTL = Duration.ofMinutes(5);

    private final RedissonClient redissonClient;

    /**
     * 좌석을 선택(임시 잠금)합니다.
     * Redis SET NX로 원자적으로 잠금하며, 이미 선택된 좌석이면 예외를 발생시킵니다.
     */
    public void select(final Long performanceId, final Long seatId, final Long memberId) {
        final String key = SeatRedisKey.select(performanceId, seatId);
        final RBucket<String> bucket = redissonClient.getBucket(key, StringCodec.INSTANCE);

        final boolean locked = bucket.setIfAbsent(memberId.toString(), SELECT_TTL);
        if (!locked) {
            log.warn("좌석 선택 실패 (이미 선택됨): performanceId={}, seatId={}, memberId={}", performanceId, seatId, memberId);
            throw new CoreException(ErrorType.SEAT_ALREADY_SELECTED);
        }

        log.info("좌석 선택 성공: performanceId={}, seatId={}, memberId={}", performanceId, seatId, memberId);
    }

    /**
     * 좌석 선택을 해제합니다.
     * 본인이 선택한 좌석만 해제할 수 있습니다.
     */
    public void deselect(final Long performanceId, final Long seatId, final Long memberId) {
        final String key = SeatRedisKey.select(performanceId, seatId);
        final RBucket<String> bucket = redissonClient.getBucket(key, StringCodec.INSTANCE);

        final String holder = bucket.get();
        if (holder == null) {
            log.info("좌석 선택 해제 무시 (이미 해제됨): performanceId={}, seatId={}", performanceId, seatId);
            return;
        }

        if (!memberId.toString().equals(holder)) {
            log.warn("좌석 해제 권한 없음: performanceId={}, seatId={}, requestMemberId={}, holderMemberId={}",
                    performanceId, seatId, memberId, holder);
            throw new CoreException(ErrorType.SEAT_NOT_OWNED);
        }

        bucket.delete();
        log.info("좌석 선택 해제 성공: performanceId={}, seatId={}, memberId={}", performanceId, seatId, memberId);
    }

    /**
     * Select → Hold 전이 시, SELECTING 키를 강제 삭제합니다.
     * 소유권 검증 없이 삭제합니다 (Hold 과정에서 이미 검증됨).
     */
    public void forceDeselect(final Long performanceId, final Long seatId) {
        redissonClient.getBucket(SeatRedisKey.select(performanceId, seatId), StringCodec.INSTANCE).delete();
    }

    /**
     * 특정 공연 회차의 현재 선택된 좌석 목록을 조회합니다.
     */
    public List<SeatSelectionInfo> getSelectedSeats(final Long performanceId) {
        final String pattern = SeatRedisKey.selectPattern(performanceId);
        final List<SeatSelectionInfo> selections = new ArrayList<>();

        for (final String key : redissonClient.getKeys().getKeysByPattern(pattern)) {
            final RBucket<String> bucket = redissonClient.getBucket(key, StringCodec.INSTANCE);
            final String memberId = bucket.get();
            if (memberId != null) {
                selections.add(new SeatSelectionInfo(SeatRedisKey.extractSeatId(key), Long.parseLong(memberId)));
            }
        }

        return selections;
    }

    /**
     * 특정 공연의 SELECTING 상태 좌석 ID 목록을 조회합니다.
     * memberId가 필요 없으므로 키 존재 여부만으로 판단합니다.
     */
    public Set<Long> getSelectingSeatIds(final Long performanceId) {
        final String pattern = SeatRedisKey.selectPattern(performanceId);
        final Set<Long> seatIds = new HashSet<>();
        for (final String key : redissonClient.getKeys().getKeysByPattern(pattern)) {
            seatIds.add(SeatRedisKey.extractSeatId(key));
        }
        return seatIds;
    }

    public record SeatSelectionInfo(Long seatId, Long memberId) {}
}
