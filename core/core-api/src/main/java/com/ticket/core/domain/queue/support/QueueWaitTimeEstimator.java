package com.ticket.core.domain.queue.support;

import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class QueueWaitTimeEstimator {

    public long estimateSeconds(final long position, final int maxActiveUsers, final Duration entryTokenTtl) {
        if (position <= 0) {
            return 0L;
        }

        final int safeMaxActiveUsers = Math.max(maxActiveUsers, 1);
        final long batch = (long) Math.ceil((double) position / safeMaxActiveUsers);
        return batch * entryTokenTtl.toSeconds();
    }
}
