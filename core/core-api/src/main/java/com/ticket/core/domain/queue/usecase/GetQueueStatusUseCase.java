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

    public record Input(Long performanceId, Long memberId, String queueEntryId) {}

    public record Output(
            QueueEntryStatus status,
            String queueEntryId,
            Long position,
            String queueToken,
            LocalDateTime expiresAt
    ) {}

    public Output execute(final Input input) {
        final QueueTicket entry = queueTicketStore.findEntry(input.queueEntryId()).orElse(null);

        if (entry == null) {
            return new Output(QueueEntryStatus.EXPIRED, input.queueEntryId(), null, null, null);
        }

        entry.assertOwnedBy(input.performanceId(), input.memberId());

        if (entry.isWaiting()) {
            final long position = queueTicketStore.findWaitingPosition(input.performanceId(), input.queueEntryId())
                    .orElse(0L);
            return new Output(entry.status(), entry.queueEntryId(), position, null, null);
        }

        if (entry.isAdmitted()) {
            final boolean validToken = entry.hasQueueToken()
                    && queueTicketStore.isValidToken(input.performanceId(), entry.queueToken());
            if (!validToken) {
                return new Output(QueueEntryStatus.EXPIRED, entry.queueEntryId(), null, null, null);
            }
        }

        return new Output(
                entry.status(),
                entry.queueEntryId(),
                null,
                entry.queueToken(),
                entry.expiresAt()
        );
    }
}
