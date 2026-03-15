package com.ticket.core.domain.queue.runtime;

import com.ticket.core.domain.queue.model.QueueEntryStatus;

import java.time.LocalDateTime;

public record QueueEntryRuntime(
        Long performanceId,
        String queueEntryId,
        QueueEntryStatus status,
        Long sequence,
        String queueToken,
        LocalDateTime expiresAt
) {
}
