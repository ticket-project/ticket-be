package com.ticket.core.domain.order.event;

import com.ticket.core.domain.hold.support.HoldManager;
import com.ticket.core.domain.performanceseat.application.SeatStatusPublishApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderTransactionEventListener {

    private final HoldManager holdManager;
    private final SeatStatusPublishApplicationService seatStatusPublishApplicationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(final OrderCancelledEvent event) {
        releaseAndPublish(event.performanceId(), event.holdKey(), event.seatIds());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(final OrderExpiredEvent event) {
        releaseAndPublish(event.performanceId(), event.holdKey(), event.seatIds());
    }

    private void releaseAndPublish(final Long performanceId, final String holdKey, final java.util.List<Long> seatIds) {
        holdManager.release(performanceId, holdKey, seatIds);
        seatStatusPublishApplicationService.publishReleased(performanceId, seatIds);
    }
}
