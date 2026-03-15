package com.ticket.core.domain.queue.usecase;

import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.domain.queue.runtime.QueueEntryRuntime;
import com.ticket.core.domain.queue.runtime.QueueRuntimeStore;
import com.ticket.core.domain.queue.support.QueuePolicyResolver;
import com.ticket.core.domain.queue.support.QueueWaitTimeEstimator;
import com.ticket.core.domain.queue.support.ResolvedQueuePolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GetQueueStatusUseCase {

    private final QueuePolicyResolver queuePolicyResolver;
    private final QueueRuntimeStore queueRuntimeStore;
    private final QueueWaitTimeEstimator queueWaitTimeEstimator;

    public record Input(Long performanceId, String queueEntryId) {}

    public record Output(
            QueueEntryStatus status,
            String queueEntryId,
            Long position,
            Long estimatedWaitSeconds,
            String queueToken,
            LocalDateTime expiresAt
    ) {}

    public Output execute(final Input input) {
        final ResolvedQueuePolicy policy = queuePolicyResolver.resolve(input.performanceId());
        final QueueEntryRuntime entry = queueRuntimeStore.findEntry(input.queueEntryId()).orElse(null);

        if (entry == null) {
            return new Output(QueueEntryStatus.EXPIRED, input.queueEntryId(), null, null, null, null);
        }

        if (entry.status() == QueueEntryStatus.WAITING) {
            final long position = queueRuntimeStore.findWaitingPosition(input.performanceId(), input.queueEntryId())
                    .orElse(0L);
            final long estimatedWaitSeconds = queueWaitTimeEstimator.estimateSeconds(
                    position,
                    policy.maxActiveUsers(),
                    policy.entryTokenTtl()
            );
            return new Output(entry.status(), entry.queueEntryId(), position, estimatedWaitSeconds, null, null);
        }

        if (entry.status() == QueueEntryStatus.ADMITTED) {
            final boolean validToken = entry.queueToken() != null
                    && queueRuntimeStore.isValidToken(input.performanceId(), entry.queueToken());
            if (!validToken) {
                return new Output(QueueEntryStatus.EXPIRED, entry.queueEntryId(), null, null, null, null);
            }
        }

        return new Output(
                entry.status(),
                entry.queueEntryId(),
                null,
                null,
                entry.queueToken(),
                entry.expiresAt()
        );
    }
}
