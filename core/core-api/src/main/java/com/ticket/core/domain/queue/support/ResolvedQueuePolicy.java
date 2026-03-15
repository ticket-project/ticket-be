package com.ticket.core.domain.queue.support;

import com.ticket.core.domain.queue.model.QueueLevel;

import java.time.Duration;

public record ResolvedQueuePolicy(
        boolean enabled,
        QueueLevel queueLevel,
        int maxActiveUsers,
        Duration entryTokenTtl,
        Duration entryRetention
) {
}
