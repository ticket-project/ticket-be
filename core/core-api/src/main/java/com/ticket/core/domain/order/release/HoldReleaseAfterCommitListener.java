package com.ticket.core.domain.order.release;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class HoldReleaseAfterCommitListener {

    private final HoldReleaseOutboxExecutor holdReleaseOutboxExecutor;
    private final Clock clock;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAfterCommit(final HoldReleaseRequestedEvent event) {
        holdReleaseOutboxExecutor.process(event.outboxId(), LocalDateTime.now(clock));
    }
}
