package com.ticket.core.domain.performanceseat;

import com.ticket.core.domain.order.OrderSeat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.ticket.core.domain.performanceseat.SeatStatusMessage.SeatAction.HELD;
import static com.ticket.core.domain.performanceseat.SeatStatusMessage.SeatAction.RELEASED;

@Service
@RequiredArgsConstructor
public class SeatStatusPublishApplicationService {

    private final SeatEventPublisher seatEventPublisher;

    public void publishHeld(final Long performanceId, final List<Long> seatIds) {
        for (final Long seatId : seatIds) {
            seatEventPublisher.publish(SeatStatusMessage.of(performanceId, seatId, HELD));
        }
    }

    public void publishReleased(final Long performanceId, final List<OrderSeat> orderSeats) {
        for (final OrderSeat orderSeat : orderSeats) {
            seatEventPublisher.publish(SeatStatusMessage.of(performanceId, orderSeat.getSeatId(), RELEASED));
        }
    }
}
