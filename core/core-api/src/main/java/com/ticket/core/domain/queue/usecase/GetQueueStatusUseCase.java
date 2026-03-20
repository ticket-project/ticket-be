package com.ticket.core.domain.queue.usecase;

import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.domain.queue.runtime.QueueTicket;
import com.ticket.core.domain.queue.runtime.QueueTicketStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GetQueueStatusUseCase {

    private final QueueTicketStore queueTicketStore;

    public record Input(Long performanceId, Long memberId, QueueEntryId queueEntryId) {}

    public record Output(
            QueueEntryStatus status,
            String queueEntryId,
            Long position,
            String queueToken,
            LocalDateTime expiresAt
    ) {}

    public Output execute(final Input input) {
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
        return queueTicketStore.findEntry(queueEntryId.value()).orElse(null);
    }

    private Output expired(final QueueEntryId queueEntryId) {
        return new Output(QueueEntryStatus.EXPIRED, queueEntryId.value(), null, null, null);
    }

    private Output waiting(final Input input, final QueueTicket entry) {
        final long position = queueTicketStore.findWaitingPosition(input.performanceId(), input.queueEntryId().value())
                .orElse(0L);
        return new Output(entry.status(), entry.queueEntryId(), position, null, null);
    }

    private boolean isExpiredAdmitted(final Input input, final QueueTicket entry) {
        if (!entry.isAdmitted()) {
            return false;
        }
        return !entry.hasQueueToken() || !queueTicketStore.isValidToken(input.performanceId(), entry.queueToken());
    }

    private Output current(final QueueTicket entry) {
        return new Output(
                entry.status(),
                entry.queueEntryId(),
                null,
                entry.queueToken(),
                entry.expiresAt()
        );
    }
}
