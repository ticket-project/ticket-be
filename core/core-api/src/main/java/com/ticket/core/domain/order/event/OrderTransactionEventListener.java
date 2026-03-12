package com.ticket.core.domain.order.event;

import com.ticket.core.domain.hold.application.HoldReleaseApplicationService;
import com.ticket.core.domain.performanceseat.application.SeatStatusPublishApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderTransactionEventListener {

    private final HoldReleaseApplicationService holdReleaseApplicationService;
    private final SeatStatusPublishApplicationService seatStatusPublishApplicationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(final OrderCancelledEvent event) {
        holdReleaseApplicationService.release(event.performanceId(), event.holdToken(), event.seatIds());
        seatStatusPublishApplicationService.publishReleased(event.performanceId(), event.seatIds());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(final OrderExpiredEvent event) {
        holdReleaseApplicationService.release(event.performanceId(), event.holdToken(), event.seatIds());
        seatStatusPublishApplicationService.publishReleased(event.performanceId(), event.seatIds());
    }
}
