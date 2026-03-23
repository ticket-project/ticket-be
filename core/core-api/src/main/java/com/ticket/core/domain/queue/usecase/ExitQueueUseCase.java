package com.ticket.core.domain.queue.usecase;

import com.ticket.core.aop.DistributedLock;
import com.ticket.core.domain.queue.command.QueueAdmissionProcessor;
import com.ticket.core.domain.queue.runtime.QueueTicket;
import com.ticket.core.domain.queue.runtime.QueueTicketStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExitQueueUseCase {

    private final QueueTicketStore queueTicketStore;
    private final QueueAdmissionProcessor queueAdmissionProcessor;

    public record Input(Long performanceId, Long memberId, QueueEntryId queueEntryId) {}

    @DistributedLock(
            prefix = "queue",
            dynamicKey = "#input.performanceId()",
            leaseTime = 5000L,
            message = "대기열 이탈 처리 중입니다. 잠시 후 다시 시도해 주세요."
    )
    public void execute(final Input input) {
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
            queueAdmissionProcessor.advance(input.performanceId());
        }
    }
}
