package com.ticket.core.domain.queue.command.exit;

import com.ticket.core.domain.queue.command.QueueAdmissionAdvancer;
import com.ticket.core.domain.queue.runtime.QueueTicket;
import com.ticket.core.domain.queue.runtime.QueueTicketStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueueExitProcessor {

    private final QueueTicketStore queueTicketStore;
    private final QueueAdmissionAdvancer queueAdmissionAdvancer;

    public void exit(final ExitQueueUseCase.Input input) {
        final QueueTicket entry = queueTicketStore.findEntry(input.queueEntryId()).orElse(null);
        if (entry == null || !input.performanceId().equals(entry.performanceId())) {
            return;
        }
        entry.assertOwnedBy(input.performanceId(), input.memberId());
        if (entry.isWaiting()) {
            queueTicketStore.leaveWaiting(input.performanceId(), input.queueEntryId());
            return;
        }
        if (entry.isAdmitted()) {
            queueTicketStore.leaveAdmitted(input.performanceId(), input.queueEntryId(), entry.queueToken());
            queueAdmissionAdvancer.advance(input.performanceId());
        }
    }
}
