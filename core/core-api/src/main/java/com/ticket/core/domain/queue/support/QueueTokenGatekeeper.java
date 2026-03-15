package com.ticket.core.domain.queue.support;

import com.ticket.core.domain.queue.runtime.QueueRuntimeStore;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueueTokenGatekeeper {

    private final QueueRuntimeStore queueRuntimeStore;

    public void assertAccessible(final Long performanceId, final String queueToken) {
        if (queueToken == null || queueToken.isBlank()) {
            throw new CoreException(ErrorType.QUEUE_TOKEN_REQUIRED);
        }
        if (!queueRuntimeStore.isValidToken(performanceId, queueToken)) {
            throw new CoreException(ErrorType.QUEUE_TOKEN_INVALID);
        }
    }
}
