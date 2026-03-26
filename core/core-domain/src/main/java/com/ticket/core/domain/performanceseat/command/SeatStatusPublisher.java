package com.ticket.core.domain.performanceseat.command;

import com.ticket.core.domain.performanceseat.support.SeatEventPublisher;
import com.ticket.core.domain.performanceseat.support.SeatStatusMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.ticket.core.domain.performanceseat.support.SeatStatusMessage.SeatAction.HELD;
import static com.ticket.core.domain.performanceseat.support.SeatStatusMessage.SeatAction.RELEASED;

@Service
@RequiredArgsConstructor
public class SeatStatusPublisher {

    private final SeatEventPublisher seatEventPublisher;

    public void publishHeld(final Long performanceId, final List<Long> seatIds) {
        for (final Long seatId : seatIds) {
            seatEventPublisher.publish(SeatStatusMessage.of(performanceId, seatId, HELD));
        }
    }

    public void publishReleased(final Long performanceId, final List<Long> seatIds) {
        for (final Long seatId : seatIds) {
            seatEventPublisher.publish(SeatStatusMessage.of(performanceId, seatId, RELEASED));
        }
    }
}
