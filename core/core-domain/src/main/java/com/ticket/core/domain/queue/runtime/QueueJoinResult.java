package com.ticket.core.domain.queue.runtime;

import com.ticket.core.domain.queue.model.QueueEntryStatus;

import java.time.LocalDateTime;

public record QueueJoinResult(
        QueueEntryStatus status,
        String queueEntryId,
        Long position,
        String queueToken,
        LocalDateTime expiresAt
) {
}
