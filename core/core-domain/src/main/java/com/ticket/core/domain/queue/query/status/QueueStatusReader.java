package com.ticket.core.domain.queue.query.status;

import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.domain.queue.runtime.QueueTicket;
import com.ticket.core.domain.queue.runtime.QueueTicketStore;
import com.ticket.core.domain.queue.model.QueueEntryId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueueStatusReader {

    private final QueueTicketStore queueTicketStore;

    public GetQueueStatusUseCase.Output read(final GetQueueStatusUseCase.Input input) {
        final QueueTicket entry = findEntry(input.queueEntryId());
        if (entry == null) {
            return expired(input.queueEntryId());
        }
        entry.assertOwnedBy(input.performanceId(), input.memberId());
        if (entry.isWaiting()) {
            return waiting(input, entry);
        }
        if (isExpiredAdmitted(input, entry)) {
            return expired(input.queueEntryId());
        }
        return current(entry);
    }

    private QueueTicket findEntry(final QueueEntryId queueEntryId) {
        return queueTicketStore.findEntry(queueEntryId).orElse(null);
    }

    private GetQueueStatusUseCase.Output expired(final QueueEntryId queueEntryId) {
        return new GetQueueStatusUseCase.Output(QueueEntryStatus.EXPIRED, queueEntryId.value(), null, null, null);
    }

    private GetQueueStatusUseCase.Output waiting(final GetQueueStatusUseCase.Input input, final QueueTicket entry) {
        final long position = queueTicketStore.findWaitingPosition(input.performanceId(), input.queueEntryId())
                .orElse(0L);
        return new GetQueueStatusUseCase.Output(entry.status(), entry.queueEntryId(), position, null, null);
    }

    private boolean isExpiredAdmitted(final GetQueueStatusUseCase.Input input, final QueueTicket entry) {
        if (!entry.isAdmitted()) {
            return false;
        }
        return !entry.hasQueueToken() || !queueTicketStore.isValidToken(input.performanceId(), entry.queueToken());
    }

    private GetQueueStatusUseCase.Output current(final QueueTicket entry) {
        return new GetQueueStatusUseCase.Output(
                entry.status(),
                entry.queueEntryId(),
                null,
                entry.queueToken(),
                entry.expiresAt()
        );
    }
}
