package com.ticket.core.domain.queue.command.join;

import com.ticket.core.domain.queue.runtime.QueueTicket;
import com.ticket.core.domain.queue.runtime.QueueTicketStore;
import com.ticket.core.domain.queue.model.QueueEntryId;
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
        if (shouldAdmitImmediately(input, policy)) {
            return admit(input, policy);
        }
        return waitInQueue(input, policy);
    }

    private boolean shouldAdmitImmediately(final JoinQueueUseCase.Input input, final QueuePolicy policy) {
        final long activeUsers = queueTicketStore.countActive(input.performanceId());
        final long waitingUsers = queueTicketStore.countWaiting(input.performanceId());
        return policy.shouldAdmitImmediately(activeUsers) && waitingUsers == 0L;
    }

    private JoinQueueUseCase.Output admit(final JoinQueueUseCase.Input input, final QueuePolicy policy) {
        final QueueTicket admitted = queueTicketStore.admitNow(
                input.performanceId(),
                input.memberId(),
                policy.entryTokenTtl(),
                policy.entryRetention(),
                LocalDateTime.now(clock)
        );
        return new JoinQueueUseCase.Output(
                admitted.status(),
                admitted.queueEntryId(),
                null,
                admitted.queueToken(),
                admitted.expiresAt()
        );
    }

    private JoinQueueUseCase.Output waitInQueue(final JoinQueueUseCase.Input input, final QueuePolicy policy) {
        final QueueTicket waiting = queueTicketStore.enqueue(
                input.performanceId(),
                input.memberId(),
                policy.entryRetention()
        );
        final long position = queueTicketStore.findWaitingPosition(input.performanceId(), QueueEntryId.from(waiting.queueEntryId()))
                .orElse(1L);
        return new JoinQueueUseCase.Output(waiting.status(), waiting.queueEntryId(), position, null, null);
    }
}
