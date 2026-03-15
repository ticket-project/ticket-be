package com.ticket.core.domain.queue.support;

import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceFinder;
import com.ticket.core.domain.queue.model.PerformanceQueuePolicy;
import com.ticket.core.domain.queue.model.QueueLevel;
import com.ticket.core.domain.queue.model.QueueMode;
import com.ticket.core.domain.queue.repository.PerformanceQueuePolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class QueuePolicyAdminService {

    private final PerformanceQueuePolicyRepository performanceQueuePolicyRepository;
    private final PerformanceFinder performanceFinder;
    private final QueueProperties queueProperties;

    public PolicyDetail get(final Long performanceId) {
        final PerformanceQueuePolicy policy = performanceQueuePolicyRepository.findByPerformanceId(performanceId).orElse(null);
        if (policy == null) {
            return new PolicyDetail(
                    performanceId,
                    QueueMode.AUTO,
                    queueProperties.getDefaultLevel(),
                    queueProperties.getDefaultMaxActiveUsers(),
                    (int) queueProperties.getDefaultEntryTokenTtl().toSeconds(),
                    null,
                    null,
                    null
            );
        }

        return PolicyDetail.from(policy, performanceId);
    }

    @Transactional
    public PolicyDetail upsert(final Long performanceId, final UpdateCommand command) {
        final Performance performance = performanceFinder.findById(performanceId);
        final PerformanceQueuePolicy policy = performanceQueuePolicyRepository.findByPerformanceId(performanceId)
                .orElseGet(() -> new PerformanceQueuePolicy(
                        performance,
                        command.queueMode(),
                        command.queueLevel(),
                        command.maxActiveUsers(),
                        command.entryTokenTtlSeconds(),
                        command.preopenQueueStartAt(),
                        command.waitingRoomMessage(),
                        command.reason()
                ));

        policy.update(
                command.queueMode(),
                command.queueLevel(),
                command.maxActiveUsers(),
                command.entryTokenTtlSeconds(),
                command.preopenQueueStartAt(),
                command.waitingRoomMessage(),
                command.reason()
        );
        final PerformanceQueuePolicy saved = performanceQueuePolicyRepository.save(policy);
        return PolicyDetail.from(saved, performanceId);
    }

    public record UpdateCommand(
            QueueMode queueMode,
            QueueLevel queueLevel,
            Integer maxActiveUsers,
            Integer entryTokenTtlSeconds,
            LocalDateTime preopenQueueStartAt,
            String waitingRoomMessage,
            String reason
    ) {
    }

    public record PolicyDetail(
            Long performanceId,
            QueueMode queueMode,
            QueueLevel queueLevel,
            Integer maxActiveUsers,
            Integer entryTokenTtlSeconds,
            LocalDateTime preopenQueueStartAt,
            String waitingRoomMessage,
            String reason
    ) {
        static PolicyDetail from(final PerformanceQueuePolicy policy, final Long performanceId) {
            return new PolicyDetail(
                    performanceId,
                    policy.getQueueMode(),
                    policy.getQueueLevel(),
                    policy.getMaxActiveUsers(),
                    policy.getEntryTokenTtlSeconds(),
                    policy.getPreopenQueueStartAt(),
                    policy.getWaitingRoomMessage(),
                    policy.getReason()
            );
        }
    }
}
