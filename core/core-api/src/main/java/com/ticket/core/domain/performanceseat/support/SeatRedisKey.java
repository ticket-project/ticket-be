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

    public static boolean isSelectKey(String key) {
        return key != null && key.startsWith("seat:select:{perf:");
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

    public static boolean isHoldMetaKey(String key) {
        return key != null && key.startsWith("hold:token:");
    }

    // ── 공통 유틸 ──
    public static Long extractPerformanceId(String key) {
        final int start = key.indexOf("{perf:");
        final int end = key.indexOf('}', start);
        if (start < 0 || end < 0) {
            throw new IllegalArgumentException("performanceId를 추출할 수 없는 key 입니다. key=" + key);
        }
        return Long.parseLong(key.substring(start + 6, end));
    }

    public static Long extractSeatId(String key) {
        return Long.parseLong(key.substring(key.lastIndexOf(':') + 1));
    }

    public static String extractHoldToken(String key) {
        return key.substring("hold:token:".length());
    }
}
