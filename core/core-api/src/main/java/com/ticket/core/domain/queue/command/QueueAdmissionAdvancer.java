package com.ticket.core.domain.queue.command;

import com.ticket.core.aop.DistributedLock;
import com.ticket.core.domain.queue.runtime.QueueTicketStore;
import com.ticket.core.domain.queue.support.QueuePolicyResolver;
import com.ticket.core.domain.queue.support.QueuePolicy;
import com.ticket.core.domain.queue.usecase.QueueEntryId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueueAdmissionAdvancer {

    private final QueuePolicyResolver queuePolicyResolver;
    private final QueueTicketStore queueTicketStore;

    @DistributedLock(
            prefix = "queue",
            dynamicKey = "#performanceId",
            leaseTime = 5000L,
            message = "대기열 승격 처리 중입니다. 잠시 후 다시 시도해 주세요."
    )
    public void advance(final Long performanceId) {
        advanceWithinLock(performanceId);
    }

    @DistributedLock(
            prefix = "queue",
            dynamicKey = "#performanceId",
            leaseTime = 5000L,
            message = "대기열 만료 처리 중입니다. 잠시 후 다시 시도해 주세요."
    )
    public void handleTokenExpired(final Long performanceId, final QueueEntryId queueEntryId, final String queueToken) {
        queueTicketStore.expireAdmitted(performanceId, queueEntryId, queueToken);
        advanceWithinLock(performanceId);
    }

    private void advanceWithinLock(final Long performanceId) {
        final QueuePolicy policy = queuePolicyResolver.resolve(performanceId);

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
