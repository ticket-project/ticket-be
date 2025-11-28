package com.ticket.util;

import java.time.LocalDateTime;

public class TestCommonUtils {

    public static LocalDateTime currentTime() {
        return LocalDateTime.now();
    }

    public static LocalDateTime performanceStartTime() {
        return LocalDateTime.of(2026, 1, 20, 10, 0, 0);
    }

    public static LocalDateTime performanceEndTime() {
        return LocalDateTime.of(2026, 1, 20, 12, 0, 0);
    }

    public static LocalDateTime reserveOpenTimeCurrent() {
        return LocalDateTime.now().minusHours(1);
    }

    public static LocalDateTime reserveCloseTimeCurrent() {
        return reserveOpenTimeCurrent().plusHours(2);
    }

    public static LocalDateTime reserveOpenTimeFuture() {
        return LocalDateTime.of(2026, 1, 1, 10, 0, 0);
    }

    public static LocalDateTime reserveCloseTimeFuture() {
        return reserveOpenTimeFuture().plusHours(1);
    }
}
