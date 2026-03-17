package com.ticket.core.domain.queue.usecase;

import com.ticket.core.aop.DistributedLock;
import com.ticket.core.domain.queue.runtime.QueueEntryRuntime;
import com.ticket.core.domain.queue.runtime.QueueEntryLifecycleService;
import com.ticket.core.domain.queue.runtime.QueueRuntimeStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LeaveQueueUseCase {

    private final QueueRuntimeStore queueRuntimeStore;
    private final QueueEntryLifecycleService queueEntryLifecycleService;

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
        queueEntryLifecycleService.leave(input.performanceId(), input.memberId(), entry);
    }
}
