package com.ticket.core.domain.hold;

import com.ticket.core.domain.order.OrderService;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performanceseat.PerformanceSeat;
import com.ticket.core.domain.performanceseat.SeatEventPublisher;
import com.ticket.core.domain.performanceseat.SeatSelectionService;
import com.ticket.core.domain.performanceseat.SeatStatusMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

import static com.ticket.core.domain.performanceseat.SeatStatusMessage.SeatAction.HELD;

@Component
@RequiredArgsConstructor
public class HoldCreator {

    private final HoldRedisService holdRedisService;
    private final OrderService orderService;
    private final SeatSelectionService seatSelectionService;
    private final SeatEventPublisher seatEventPublisher;

    public Long create(
            final Long memberId,
            final Long performanceId,
            final Performance performance,
            final List<Long> seatIds,
            final List<PerformanceSeat> performanceSeats
    ) {
        final Duration ttl = Duration.ofSeconds(performance.getHoldTime());
        final HoldRedisService.HoldSnapshot snapshot = holdRedisService.createHold(memberId, performanceId, seatIds, ttl);

        try {
            final Long orderId = orderService.createPendingOrder(
                    memberId,
                    performanceId,
                    snapshot.holdToken(),
                    snapshot.expiresAt(),
                    performanceSeats
            );
            for (final Long seatId : seatIds) {
                seatSelectionService.forceDeselect(performanceId, seatId);
                seatEventPublisher.publish(SeatStatusMessage.of(performanceId, seatId, HELD));
            }
            return orderId;
        } catch (final RuntimeException e) {
            holdRedisService.releaseHold(snapshot.performanceId(), snapshot.holdToken(), snapshot.seatIds());
            throw e;
        }
    }
}
