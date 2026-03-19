package com.ticket.core.domain.queue.command;

import com.ticket.core.aop.DistributedLock;
import com.ticket.core.domain.queue.runtime.QueueTicketStore;
import com.ticket.core.domain.queue.support.QueuePolicyResolver;
import com.ticket.core.domain.queue.support.ResolvedQueuePolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueueAdmissionProcessor {

    private final QueuePolicyResolver queuePolicyResolver;
    private final QueueTicketStore queueTicketStore;

    @DistributedLock(
            prefix = "queue-enter",
            dynamicKey = "#performanceId",
            leaseTime = 5000L,
            message = "대기열 승격 처리 중입니다. 잠시 후 다시 시도해 주세요."
    )
    public void advance(final Long performanceId) {
        advanceWithinLock(performanceId);
    }

    @DistributedLock(
            prefix = "queue-enter",
            dynamicKey = "#performanceId",
            leaseTime = 5000L,
            message = "대기열 만료 처리 중입니다. 잠시 후 다시 시도해 주세요."
    )
    public void handleTokenExpired(final Long performanceId, final String queueEntryId, final String queueToken) {
        queueTicketStore.expireAdmitted(performanceId, queueEntryId, queueToken);
        advanceWithinLock(performanceId);
    }

    private void advanceWithinLock(final Long performanceId) {
        final ResolvedQueuePolicy policy = queuePolicyResolver.resolve(performanceId);

        while (queueTicketStore.countActive(performanceId) < policy.maxActiveUsers()) {
            final boolean admitted = queueTicketStore.admitNextWaiting(
                    performanceId,
                    policy.entryTokenTtl(),
                    policy.entryRetention()
            ).isPresent();
            if (!admitted) {
                return;
            }
        }
    }
}
