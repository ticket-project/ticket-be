package com.ticket.core.domain.queue.usecase;

import com.ticket.core.aop.DistributedLock;
import com.ticket.core.domain.queue.command.QueueAdvanceProcessor;
import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.domain.queue.runtime.QueueEntryRuntime;
import com.ticket.core.domain.queue.runtime.QueueRuntimeStore;
import com.ticket.core.domain.queue.support.QueuePolicyResolver;
import com.ticket.core.domain.queue.support.ResolvedQueuePolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EnterQueueEntryUseCase {

    private final QueuePolicyResolver queuePolicyResolver;
    private final QueueRuntimeStore queueRuntimeStore;
    private final QueueAdvanceProcessor queueAdvanceProcessor;

    public record Input(Long performanceId, Long memberId) {}

    public record Output(
            QueueEntryStatus status,
            String queueEntryId,
            Long position,
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
        cleanupForReentry(input.performanceId(), input.memberId());

        final long activeUsers = queueRuntimeStore.countActive(input.performanceId());
        final long waitingUsers = queueRuntimeStore.countWaiting(input.performanceId());
        if (policy.shouldAdmitImmediately(activeUsers) && waitingUsers == 0L) {
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
        return new Output(
                waiting.status(),
                waiting.queueEntryId(),
                position,
                null,
                null
        );
    }

    private void cleanupForReentry(final Long performanceId, final Long memberId) {
        final String existingQueueEntryId = queueRuntimeStore.findMemberEntryId(performanceId, memberId).orElse(null);
        if (existingQueueEntryId == null) {
            return;
        }

        final QueueEntryRuntime existingEntry = queueRuntimeStore.findEntry(existingQueueEntryId).orElse(null);
        if (existingEntry == null) {
            queueRuntimeStore.clearMemberEntry(performanceId, memberId);
            return;
        }

        if (existingEntry.isWaiting()) {
            queueRuntimeStore.leaveWaiting(performanceId, existingQueueEntryId);
            return;
        }

        if (existingEntry.isOwnedBy(performanceId, memberId) && existingEntry.isAdmitted()) {
            queueRuntimeStore.leaveAdmitted(performanceId, existingQueueEntryId, existingEntry.queueToken());
            queueAdvanceProcessor.advance(performanceId);
            return;
        }

        queueRuntimeStore.clearMemberEntry(performanceId, memberId);
    }
}
