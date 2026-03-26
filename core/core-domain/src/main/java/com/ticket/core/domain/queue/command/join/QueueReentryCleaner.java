package com.ticket.core.domain.queue.command.join;

import com.ticket.core.domain.queue.command.QueueAdmissionAdvancer;
import com.ticket.core.domain.queue.runtime.QueueTicket;
import com.ticket.core.domain.queue.runtime.QueueTicketStore;
import com.ticket.core.domain.queue.model.QueueEntryId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueueReentryCleaner {

    private final QueueTicketStore queueTicketStore;
    private final QueueAdmissionAdvancer queueAdmissionAdvancer;

    public void cleanup(final Long performanceId, final Long memberId) {
        final String existingQueueEntryId = queueTicketStore.findMemberEntryId(performanceId, memberId).orElse(null);
        if (existingQueueEntryId == null) {
            return;
        }
        final QueueEntryId queueEntryId = QueueEntryId.from(existingQueueEntryId);
        final QueueTicket existingEntry = queueTicketStore.findEntry(queueEntryId).orElse(null);
        if (existingEntry == null) {
            clearMemberEntry(performanceId, memberId);
            return;
        }
        if (existingEntry.isWaiting()) {
            leaveWaiting(performanceId, queueEntryId);
            return;
        }
        if (existingEntry.isOwnedBy(performanceId, memberId) && existingEntry.isAdmitted()) {
            leaveAdmitted(performanceId, queueEntryId, existingEntry.queueToken());
            return;
        }
        clearMemberEntry(performanceId, memberId);
    }

    private void leaveWaiting(final Long performanceId, final QueueEntryId queueEntryId) {
        queueTicketStore.leaveWaiting(performanceId, queueEntryId);
    }

    private void leaveAdmitted(final Long performanceId, final QueueEntryId queueEntryId, final String queueToken) {
        queueTicketStore.leaveAdmitted(performanceId, queueEntryId, queueToken);
        queueAdmissionAdvancer.advance(performanceId);
    }

    private void clearMemberEntry(final Long performanceId, final Long memberId) {
        queueTicketStore.clearMemberEntry(performanceId, memberId);
    }
}
