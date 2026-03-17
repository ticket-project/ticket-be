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

    public boolean shouldAdmitImmediately(final long activeUsers) {
        return !enabled || activeUsers < maxActiveUsers;
    }

    public long estimateWaitSeconds(final long position) {
        if (position <= 0) {
            return 0L;
        }

        final int safeMaxActiveUsers = Math.max(maxActiveUsers, 1);
        final long batch = (long) Math.ceil((double) position / safeMaxActiveUsers);
        return batch * entryTokenTtl.toSeconds();
    }
}
