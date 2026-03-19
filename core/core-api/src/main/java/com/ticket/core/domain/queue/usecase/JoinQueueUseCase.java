package com.ticket.core.domain.queue.usecase;

import com.ticket.core.aop.DistributedLock;
import com.ticket.core.domain.queue.command.QueueAdmissionProcessor;
import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.domain.queue.runtime.QueueTicket;
import com.ticket.core.domain.queue.runtime.QueueTicketStore;
import com.ticket.core.domain.queue.support.QueuePolicyResolver;
import com.ticket.core.domain.queue.support.ResolvedQueuePolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class JoinQueueUseCase {

    private final QueuePolicyResolver queuePolicyResolver;
    private final QueueTicketStore queueTicketStore;
    private final QueueAdmissionProcessor queueAdmissionProcessor;

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

        final long activeUsers = queueTicketStore.countActive(input.performanceId());
        final long waitingUsers = queueTicketStore.countWaiting(input.performanceId());
        if (policy.shouldAdmitImmediately(activeUsers) && waitingUsers == 0L) {
            final QueueTicket admitted = queueTicketStore.admitNow(
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

        final QueueTicket waiting = queueTicketStore.enqueue(
                input.performanceId(),
                input.memberId(),
                policy.entryRetention()
        );
        final long position = queueTicketStore.findWaitingPosition(input.performanceId(), waiting.queueEntryId())
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
        final String existingQueueEntryId = queueTicketStore.findMemberEntryId(performanceId, memberId).orElse(null);
        if (existingQueueEntryId == null) {
            return;
        }

        final QueueTicket existingEntry = queueTicketStore.findEntry(existingQueueEntryId).orElse(null);
        if (existingEntry == null) {
            queueTicketStore.clearMemberEntry(performanceId, memberId);
            return;
        }

        if (existingEntry.isWaiting()) {
            queueTicketStore.leaveWaiting(performanceId, existingQueueEntryId);
            return;
        }

        if (existingEntry.isOwnedBy(performanceId, memberId) && existingEntry.isAdmitted()) {
            queueTicketStore.leaveAdmitted(performanceId, existingQueueEntryId, existingEntry.queueToken());
            queueAdmissionProcessor.advance(performanceId);
            return;
        }

        queueTicketStore.clearMemberEntry(performanceId, memberId);
    }
}
