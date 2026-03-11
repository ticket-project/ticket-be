package com.ticket.core.domain.performanceseat.support;

/**
 * 좌석 관련 Redis 키를 중앙에서 관리합니다.
 * 키 포맷 변경 시 이 클래스만 수정하면 됩니다.
 */
public final class SeatRedisKey {

    private SeatRedisKey() {}

    // ── Select 키 ──
    private static final String SELECT_KEY = "seat:select:{perf:%d}:%d";
    private static final String SELECT_PATTERN = "seat:select:{perf:%d}:*";

    public static String select(Long perfId, Long seatId) {
        return String.format(SELECT_KEY, perfId, seatId);
    }

    public static String selectPattern(Long perfId) {
        return String.format(SELECT_PATTERN, perfId);
    }

    // ── Hold 키 ──
    private static final String HOLD_KEY = "seat:hold:{perf:%d}:%d";
    private static final String HOLD_PATTERN = "seat:hold:{perf:%d}:*";
    private static final String HOLD_META_KEY = "hold:token:%s";

    public static String hold(Long perfId, Long seatId) {
        return String.format(HOLD_KEY, perfId, seatId);
    }

    public static String holdPattern(Long perfId) {
        return String.format(HOLD_PATTERN, perfId);
    }

    public static String holdMeta(String holdToken) {
        return String.format(HOLD_META_KEY, holdToken);
    }

    // ── 공통 유틸 ──
    public static Long extractSeatId(String key) {
        return Long.parseLong(key.substring(key.lastIndexOf(':') + 1));
    }
}
