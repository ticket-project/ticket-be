package com.ticket.core.domain.hold;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class HoldInfo {
    private final Long memberId;
    private final Long performanceId;
    private final List<Long> seatIds;
    private final LocalDateTime expiredAt;

    public HoldInfo(final Long memberId, final Long performanceId, final List<Long> seatIds, final LocalDateTime expiredAt) {
        this.memberId = memberId;
        this.performanceId = performanceId;
        this.seatIds = seatIds;
        this.expiredAt = expiredAt;
    }

}
