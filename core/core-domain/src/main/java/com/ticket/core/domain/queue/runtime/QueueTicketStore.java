package com.ticket.core.domain.queue.runtime;

import com.ticket.core.domain.queue.model.QueueEntryId;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

public interface QueueTicketStore {

    long countActive(Long performanceId);

    long countWaiting(Long performanceId);

    QueueTicket admitNow(Long performanceId, Long memberId, Duration entryTokenTtl, Duration entryRetention, LocalDateTime now);

    QueueTicket enqueue(Long performanceId, Long memberId, Duration entryRetention);

    Optional<String> findMemberEntryId(Long performanceId, Long memberId);

    void clearMemberEntry(Long performanceId, Long memberId);

    Optional<Long> findWaitingPosition(Long performanceId, QueueEntryId queueEntryId);

    Optional<QueueTicket> findEntry(QueueEntryId queueEntryId);

    boolean isValidToken(Long performanceId, String queueToken);

    Optional<QueueTicket> admitNextWaiting(Long performanceId, Duration entryTokenTtl, Duration entryRetention, LocalDateTime now);

    void expireAdmitted(Long performanceId, QueueEntryId queueEntryId, String queueToken);

    void leaveWaiting(Long performanceId, QueueEntryId queueEntryId);

    void leaveAdmitted(Long performanceId, QueueEntryId queueEntryId, String queueToken);
}
