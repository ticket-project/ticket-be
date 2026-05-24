package com.ticket.core.domain.queue.command.join;

import com.ticket.core.support.lock.DistributedLock;
import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.domain.queue.support.QueuePolicy;
import com.ticket.core.domain.queue.support.QueuePolicyResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class JoinQueueUseCase {

    private final QueuePolicyResolver queuePolicyResolver;
    private final QueueReentryCleaner queueReentryCleaner;
    private final QueueJoinProcessor queueJoinProcessor;

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
        queueReentryCleaner.cleanup(input.performanceId(), input.memberId());
        return queueJoinProcessor.join(input, policy);
    }
}
