package com.ticket.core.domain.queue.usecase;

import com.ticket.core.aop.DistributedLock;
import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.domain.queue.runtime.QueueEntryRuntime;
import com.ticket.core.domain.queue.runtime.QueueEntryLifecycleService;
import com.ticket.core.domain.queue.runtime.QueueRuntimeStore;
import com.ticket.core.domain.queue.support.QueuePolicyResolver;
import com.ticket.core.domain.queue.support.ResolvedQueuePolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class QueueEntryUseCase {

    private final QueuePolicyResolver queuePolicyResolver;
    private final QueueRuntimeStore queueRuntimeStore;
    private final QueueEntryLifecycleService queueEntryLifecycleService;

    public record Input(Long performanceId, Long memberId) {}

    public record Output(
            QueueEntryStatus status,
            String queueEntryId,
            Long position,
            Long estimatedWaitSeconds,
            String queueToken,
            LocalDateTime expiresAt
    ) {}

    @DistributedLock(
            prefix = "queue-enter",
            dynamicKey = "#input.performanceId()",
            leaseTime = 5000L,
            message = "대기열 진입 처리 중입니다. 잠시 후 다시 시도해 주세요."
    )
    public Output execute(final Input input) {
        final ResolvedQueuePolicy policy = queuePolicyResolver.resolve(input.performanceId());
        queueEntryLifecycleService.cleanupForReentry(input.performanceId(), input.memberId());

        if (policy.shouldAdmitImmediately(queueRuntimeStore.countActive(input.performanceId()))) {
            final QueueEntryRuntime admitted = queueRuntimeStore.admitNow(
                    input.performanceId(),
                    input.memberId(),
                    policy.entryTokenTtl(),
                    policy.entryRetention()
            );
            return new Output(
                    admitted.status(),
                    admitted.queueEntryId(),
                    null,
                    null,
                    admitted.queueToken(),
                    admitted.expiresAt()
            );
        }

        final QueueEntryRuntime waiting = queueRuntimeStore.enqueue(
                input.performanceId(),
                input.memberId(),
                policy.entryRetention()
        );
        final long position = queueRuntimeStore.findWaitingPosition(input.performanceId(), waiting.queueEntryId())
                .orElse(1L);
        final long estimatedWaitSeconds = policy.estimateWaitSeconds(position);
        return new Output(
                waiting.status(),
                waiting.queueEntryId(),
                position,
                estimatedWaitSeconds,
                null,
                null
        );
    }
}
