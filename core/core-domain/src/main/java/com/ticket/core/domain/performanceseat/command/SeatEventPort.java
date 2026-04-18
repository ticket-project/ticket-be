package com.ticket.core.domain.performanceseat.command;

import com.ticket.core.domain.performanceseat.support.SeatStatusMessage.SeatAction;

public interface SeatEventPort {

    void publish(Long performanceId, Long seatId, SeatAction action);
}
