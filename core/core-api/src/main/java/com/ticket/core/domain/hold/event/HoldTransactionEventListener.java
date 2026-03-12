package com.ticket.core.domain.hold.event;

import com.ticket.core.domain.hold.application.HoldReleaseApplicationService;
import com.ticket.core.domain.hold.model.HoldSnapshot;
import com.ticket.core.domain.performanceseat.application.SeatStatusPublishApplicationService;
import com.ticket.core.domain.performanceseat.command.SeatSelectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class HoldTransactionEventListener {

    private final HoldReleaseApplicationService holdReleaseApplicationService;
    private final SeatSelectionService seatSelectionService;
    private final SeatStatusPublishApplicationService seatStatusPublishApplicationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAfterCommit(final HoldCreatedEvent event) {
        final HoldSnapshot snapshot = event.snapshot();
        for (final Long seatId : snapshot.seatIds()) {
            seatSelectionService.forceDeselect(snapshot.performanceId(), seatId);
        }
        seatStatusPublishApplicationService.publishHeld(snapshot.performanceId(), snapshot.seatIds());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleAfterRollback(final HoldCreatedEvent event) {
        final HoldSnapshot snapshot = event.snapshot();
        holdReleaseApplicationService.release(
                snapshot.performanceId(),
                snapshot.holdToken(),
                snapshot.seatIds()
        );
    }
}
