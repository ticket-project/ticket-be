package com.ticket.core.domain.hold.event;

import com.ticket.core.domain.hold.model.HoldSnapshot;
import com.ticket.core.domain.hold.support.HoldManager;
import com.ticket.core.domain.performanceseat.application.SeatStatusPublisher;
import com.ticket.core.domain.performanceseat.command.SeatSelectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class HoldTransactionEventListener {

    private final HoldManager holdManager;
    private final SeatSelectionService seatSelectionService;
    private final SeatStatusPublisher seatStatusPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAfterCommit(final HoldCreatedEvent event) {
        final HoldSnapshot snapshot = event.snapshot();
        for (final Long seatId : snapshot.seatIds()) {
            seatSelectionService.forceDeselect(snapshot.performanceId(), seatId);
        }
        seatStatusPublisher.publishHeld(snapshot.performanceId(), snapshot.seatIds());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleAfterRollback(final HoldCreatedEvent event) {
        final HoldSnapshot snapshot = event.snapshot();
        holdManager.release(
                snapshot.performanceId(),
                snapshot.holdKey(),
                snapshot.seatIds()
        );
    }
}
