package com.ticket.core.domain.queue.runtime;

import java.time.Duration;
import java.util.Optional;

public interface QueueTicketStore {

    long countActive(Long performanceId);

    long countWaiting(Long performanceId);

    QueueTicket admitNow(Long performanceId, Long memberId, Duration entryTokenTtl, Duration entryRetention);

    QueueTicket enqueue(Long performanceId, Long memberId, Duration entryRetention);

    Optional<String> findMemberEntryId(Long performanceId, Long memberId);

    void clearMemberEntry(Long performanceId, Long memberId);

    Optional<Long> findWaitingPosition(Long performanceId, String queueEntryId);

    Optional<QueueTicket> findEntry(String queueEntryId);

    boolean isValidToken(Long performanceId, String queueToken);

    Optional<QueueTicket> admitNextWaiting(Long performanceId, Duration entryTokenTtl, Duration entryRetention);

    void expireAdmitted(Long performanceId, String queueEntryId, String queueToken);

    void leaveWaiting(Long performanceId, String queueEntryId);

    void leaveAdmitted(Long performanceId, String queueEntryId, String queueToken);
}
