package com.ticket.core.domain.order.domainservice;

import com.ticket.core.domain.hold.application.HoldHistoryRecorder;
import com.ticket.core.domain.hold.model.HoldSnapshot;
import com.ticket.core.domain.hold.support.HoldManager;
import com.ticket.core.domain.hold.support.HoldSeatAvailabilityValidator;
import com.ticket.core.domain.order.application.CreateOrderApplicationService;
import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.performanceseat.model.PerformanceSeat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderStartDomainService {

    private final HoldSeatAvailabilityValidator holdSeatAvailabilityValidator;
    private final HoldManager holdManager;
    private final HoldHistoryRecorder holdHistoryRecorder;
    private final CreateOrderApplicationService createOrderApplicationService;

    public OrderStartResult start(
            final Long memberId,
            final Long performanceId,
            final List<Long> seatIds,
            final Duration holdDuration
    ) {
        final List<PerformanceSeat> performanceSeats = holdSeatAvailabilityValidator.validate(performanceId, seatIds);
        final HoldSnapshot snapshot = holdManager.createHold(memberId, performanceId, seatIds, holdDuration);
        try {
            final Order order = createOrderApplicationService.createPendingOrder(
                    memberId,
                    performanceId,
                    snapshot.holdKey(),
                    snapshot.expiresAt(),
                    performanceSeats
            );
            holdHistoryRecorder.recordActiveHold(
                    memberId,
                    performanceId,
                    snapshot.holdKey(),
                    snapshot.expiresAt(),
                    performanceSeats
            );
            return new OrderStartResult(order.getOrderKey(), snapshot);
        } catch (final RuntimeException e) {
            holdManager.release(performanceId, snapshot.holdKey(), snapshot.seatIds());
            throw e;
        }
    }

    public record OrderStartResult(String orderKey, HoldSnapshot snapshot) {
    }
}
