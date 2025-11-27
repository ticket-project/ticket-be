package com.ticket.core.domain.performance;

import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;

import java.time.LocalDateTime;

public class Performance {

    private final LocalDateTime startTime; //TODO 나중에 회차 시간이 변경될 수 있지 않을까?
    private final LocalDateTime endTime;

    public Performance(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime.isAfter(endTime)) {
            throw new CoreException(ErrorType.IS_NOT_VALID_PERFORMANCE);
        }
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean isPastPerformance(LocalDateTime now) {
        return now.isAfter(endTime);
    }
}
