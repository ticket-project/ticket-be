package com.ticket.core.domain.queue.support;

import com.ticket.core.domain.queue.runtime.QueueTicketStore;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueueTokenGatekeeper {

    private final QueueTicketStore queueTicketStore;

    public void assertAccessible(final Long performanceId, final String queueToken) {
        if (queueToken == null || queueToken.isBlank()) {
            throw new CoreException(ErrorType.QUEUE_TOKEN_REQUIRED);
        }
        if (!queueTicketStore.isValidToken(performanceId, queueToken)) {
            throw new CoreException(ErrorType.QUEUE_TOKEN_INVALID);
        }
    }
}
