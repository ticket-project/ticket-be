package com.ticket.core.domain.queue.command.join;

import com.ticket.core.domain.queue.runtime.QueueJoinResult;
import com.ticket.core.domain.queue.runtime.QueueTicketStore;
import com.ticket.core.domain.queue.support.QueuePolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class QueueJoinProcessor {

    private final QueueTicketStore queueTicketStore;
    private final Clock clock;

    public JoinQueueUseCase.Output join(final JoinQueueUseCase.Input input, final QueuePolicy policy) {
        final QueueJoinResult result = queueTicketStore.enter(
                input.performanceId(),
                input.memberId(),
                policy,
                LocalDateTime.now(clock)
        );
        return new JoinQueueUseCase.Output(
                result.status(),
                result.queueEntryId(),
                result.position(),
                result.queueToken(),
                result.expiresAt()
        );
    }
}
