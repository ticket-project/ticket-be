package com.ticket.core.domain.hold.application;

import com.ticket.core.domain.hold.support.HoldRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HoldReleaseApplicationService {

    private final HoldRedisService holdRedisService;

    public void release(final Long performanceId, final String holdToken, final List<Long> seatIds) {
        holdRedisService.releaseHold(performanceId, holdToken, seatIds);
    }
}
