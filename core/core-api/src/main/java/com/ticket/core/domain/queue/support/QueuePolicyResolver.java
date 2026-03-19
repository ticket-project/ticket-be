package com.ticket.core.domain.queue.support;

import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceFinder;
import com.ticket.core.domain.queue.model.QueueLevel;
import com.ticket.core.domain.queue.model.QueueMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class QueuePolicyResolver {

    private final PerformanceFinder performanceFinder;
    private final QueueProperties queueProperties;

    public ResolvedQueuePolicy resolve(final Long performanceId) {
        final Performance performance = performanceFinder.findById(performanceId);

        final boolean enabled = resolveEnabled(performance.getQueueMode());
        final QueueLevel queueLevel = performance.getQueueLevel() != null
                ? performance.getQueueLevel()
                : queueProperties.getDefaultLevel();
        final int maxActiveUsers = performance.getMaxActiveUsers() != null
                ? performance.getMaxActiveUsers()
                : queueProperties.getDefaultMaxActiveUsers();
        final Duration entryTokenTtl = performance.getEntryTokenTtlSeconds() != null
                ? Duration.ofSeconds(performance.getEntryTokenTtlSeconds())
                : queueProperties.getDefaultEntryTokenTtl();

        return new ResolvedQueuePolicy(
                enabled,
                queueLevel,
                maxActiveUsers,
                entryTokenTtl,
                queueProperties.getEntryRetention()
        );
    }

    private boolean resolveEnabled(final QueueMode queueMode) {
        if (queueMode == null) {
            return queueProperties.isEnabledByDefault();
        }
        if (queueMode == QueueMode.FORCE_OFF) {
            return false;
        }
        if (queueMode == QueueMode.FORCE_ON) {
            return true;
        }
        return queueProperties.isEnabledByDefault();
    }
}
