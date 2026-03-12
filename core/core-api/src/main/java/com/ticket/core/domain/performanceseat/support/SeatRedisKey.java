package com.ticket.core.domain.performanceseat.support;

/**
 * 좌석 관련 Redis key 규칙을 한 곳에서 관리한다.
 */
public final class SeatRedisKey {

    private static final String SELECT_KEY = "seat:select:{perf:%d}:%d";
    private static final String SELECT_PATTERN = "seat:select:{perf:%d}:*";

    private static final String HOLD_KEY = "seat:hold:{perf:%d}:%d";
    private static final String HOLD_PATTERN = "seat:hold:{perf:%d}:*";
    private static final String HOLD_META_KEY = "hold:key:%s";

    private SeatRedisKey() {}

    public static String select(final Long perfId, final Long seatId) {
        return String.format(SELECT_KEY, perfId, seatId);
    }

    public static String selectPattern(final Long perfId) {
        return String.format(SELECT_PATTERN, perfId);
    }

    public static boolean isSelectKey(final String key) {
        return key != null && key.startsWith("seat:select:{perf:");
    }

    public static String hold(final Long perfId, final Long seatId) {
        return String.format(HOLD_KEY, perfId, seatId);
    }

    public static String holdPattern(final Long perfId) {
        return String.format(HOLD_PATTERN, perfId);
    }

    public static String holdMeta(final String holdKey) {
        return String.format(HOLD_META_KEY, holdKey);
    }

    public static boolean isHoldMetaKey(final String key) {
        return key != null && key.startsWith("hold:key:");
    }

    public static Long extractPerformanceId(final String key) {
        final int start = key.indexOf("{perf:");
        final int end = key.indexOf('}', start);
        if (start < 0 || end < 0) {
            throw new IllegalArgumentException("performanceId를 추출할 수 없는 key 입니다. key=" + key);
        }
        return Long.parseLong(key.substring(start + 6, end));
    }

    public static Long extractSeatId(final String key) {
        return Long.parseLong(key.substring(key.lastIndexOf(':') + 1));
    }

    public static String extractHoldKey(final String key) {
        return key.substring("hold:key:".length());
    }
}
