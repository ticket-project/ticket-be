package com.ticket.core.domain.performanceseat.support;

import com.ticket.core.domain.performanceseat.support.SeatStatusMessage.SeatAction;

public interface SeatStatusEventPublisher {

    void publish(Long performanceId, Long seatId, SeatAction action);

    void publish(SeatStatusMessage message);
}
