package com.ticket.core.domain.queue.usecase;

import com.ticket.core.aop.DistributedLock;
import com.ticket.core.domain.queue.command.QueueAdmissionAdvancer;
import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.domain.queue.runtime.QueueTicket;
import com.ticket.core.domain.queue.runtime.QueueTicketStore;
import com.ticket.core.domain.queue.support.QueuePolicyResolver;
import com.ticket.core.domain.queue.support.QueuePolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class JoinQueueUseCase {

    private final QueuePolicyResolver queuePolicyResolver;
    private final QueueTicketStore queueTicketStore;
    private final QueueAdmissionAdvancer queueAdmissionAdvancer;

    public record Input(Long performanceId, Long memberId) {}

    public record Output(
            QueueEntryStatus status,
            String queueEntryId,
            Long position,
            String queueToken,
            LocalDateTime expiresAt
    ) {}

    @DistributedLock(
            prefix = "queue",
            dynamicKey = "#input.performanceId()",
            leaseTime = 5000L,
            message = "대기열 진입 처리 중입니다. 잠시 후 다시 시도해 주세요."
    )
    public Output execute(final Input input) {
        final QueuePolicy policy = queuePolicyResolver.resolve(input.performanceId());
        cleanupForReentry(input.performanceId(), input.memberId());
        if (shouldAdmitImmediately(input, policy)) {
            return admit(input, policy);
        }
        return waitInQueue(input, policy);
    }

    private void cleanupForReentry(final Long performanceId, final Long memberId) {
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

    private boolean shouldAdmitImmediately(final Input input, final QueuePolicy policy) {
        final long activeUsers = queueTicketStore.countActive(input.performanceId());
        final long waitingUsers = queueTicketStore.countWaiting(input.performanceId());
        return policy.shouldAdmitImmediately(activeUsers) && waitingUsers == 0L;
    }

    private Output admit(final Input input, final QueuePolicy policy) {
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

    private Output waitInQueue(final Input input, final QueuePolicy policy) {
        final QueueTicket waiting = queueTicketStore.enqueue(
                input.performanceId(),
                input.memberId(),
                policy.entryRetention()
        );
        final long position = queueTicketStore.findWaitingPosition(input.performanceId(), QueueEntryId.from(waiting.queueEntryId()))
                .orElse(1L);
        return new Output(waiting.status(), waiting.queueEntryId(), position, null, null);
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
