package com.ticket.core.domain.queue.runtime;

import java.time.Duration;
import java.util.Optional;

public interface QueueRuntimeStore {

    long countActive(Long performanceId);

    QueueEntryRuntime admitNow(Long performanceId, Duration entryTokenTtl, Duration entryRetention);

    QueueEntryRuntime enqueue(Long performanceId, Duration entryRetention);

    Optional<Long> findWaitingPosition(Long performanceId, String queueEntryId);

    Optional<QueueEntryRuntime> findEntry(String queueEntryId);

    boolean isValidToken(Long performanceId, String queueToken);

    Optional<QueueEntryRuntime> admitNextWaiting(Long performanceId, Duration entryTokenTtl, Duration entryRetention);

    void expireAdmitted(Long performanceId, String queueEntryId, String queueToken);

    void leaveWaiting(Long performanceId, String queueEntryId);

    void leaveAdmitted(Long performanceId, String queueEntryId, String queueToken);
}
