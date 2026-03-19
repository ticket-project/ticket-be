package com.ticket.core.domain.queue.usecase;

import com.ticket.core.aop.DistributedLock;
import com.ticket.core.domain.queue.command.QueueAdvanceProcessor;
import com.ticket.core.domain.queue.runtime.QueueEntryRuntime;
import com.ticket.core.domain.queue.runtime.QueueRuntimeStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LeaveQueueUseCase {

    private final QueueRuntimeStore queueRuntimeStore;
    private final QueueAdvanceProcessor queueAdvanceProcessor;

    public record Input(Long performanceId, Long memberId, String queueEntryId) {}

    @DistributedLock(
            prefix = "queue-leave",
            dynamicKey = "#input.performanceId()",
            leaseTime = 5000L,
            message = "대기열 이탈 처리 중입니다. 잠시 후 다시 시도해 주세요."
    )
    public void execute(final Input input) {
        final QueueEntryRuntime entry = queueRuntimeStore.findEntry(input.queueEntryId()).orElse(null);
        if (entry == null || !input.performanceId().equals(entry.performanceId())) {
            return;
        }
        entry.assertOwnedBy(input.performanceId(), input.memberId());
        if (entry.isWaiting()) {
            queueRuntimeStore.leaveWaiting(input.performanceId(), input.queueEntryId());
            return;
        }
        if (entry.isAdmitted()) {
            queueRuntimeStore.leaveAdmitted(input.performanceId(), input.queueEntryId(), entry.queueToken());
            queueAdvanceProcessor.advance(input.performanceId());
        }
    }
}
