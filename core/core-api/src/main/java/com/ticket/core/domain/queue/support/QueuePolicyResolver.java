package com.ticket.core.domain.queue.support;

import com.ticket.core.domain.performance.PerformanceFinder;
import com.ticket.core.domain.queue.model.PerformanceQueuePolicy;
import com.ticket.core.domain.queue.model.QueueLevel;
import com.ticket.core.domain.queue.model.QueueMode;
import com.ticket.core.domain.queue.repository.PerformanceQueuePolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class QueuePolicyResolver {

    private final PerformanceFinder performanceFinder;
    private final PerformanceQueuePolicyRepository performanceQueuePolicyRepository;
    private final QueueProperties queueProperties;

    public ResolvedQueuePolicy resolve(final Long performanceId) {
        performanceFinder.findById(performanceId);

        final PerformanceQueuePolicy policy = performanceQueuePolicyRepository.findByPerformanceId(performanceId)
                .orElse(null);

        final boolean enabled = resolveEnabled(policy);
        final QueueLevel queueLevel = policy != null && policy.getQueueLevel() != null
                ? policy.getQueueLevel()
                : queueProperties.getDefaultLevel();
        final int maxActiveUsers = policy != null && policy.getMaxActiveUsers() != null
                ? policy.getMaxActiveUsers()
                : queueProperties.getDefaultMaxActiveUsers();
        final Duration entryTokenTtl = policy != null && policy.getEntryTokenTtlSeconds() != null
                ? Duration.ofSeconds(policy.getEntryTokenTtlSeconds())
                : queueProperties.getDefaultEntryTokenTtl();

        return new ResolvedQueuePolicy(
                enabled,
                queueLevel,
                maxActiveUsers,
                entryTokenTtl,
                queueProperties.getEntryRetention()
        );
    }

    private boolean resolveEnabled(final PerformanceQueuePolicy policy) {
        if (policy == null) {
            return queueProperties.isEnabledByDefault();
        }
        if (policy.getQueueMode() == QueueMode.FORCE_OFF) {
            return false;
        }
        if (policy.getQueueMode() == QueueMode.FORCE_ON) {
            return true;
        }
        return queueProperties.isEnabledByDefault();
    }
}
