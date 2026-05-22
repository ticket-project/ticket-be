package com.ticket.core.domain.queue.command.join;

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
    private final QueueJoinProcessor queueJoinProcessor;

    public record Input(Long performanceId, Long memberId) {}

    public record Output(
            QueueEntryStatus status,
            String queueEntryId,
            Long position,
            String queueToken,
            LocalDateTime expiresAt
    ) {}

    public Output execute(final Input input) {
        final QueuePolicy policy = queuePolicyResolver.resolve(input.performanceId());
        return queueJoinProcessor.join(input, policy);
    }
}
