package com.ticket.core.domain.queue.runtime;

import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.support.random.UuidSupplier;
import lombok.RequiredArgsConstructor;
import org.redisson.api.*;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisQueueRuntimeStore implements QueueRuntimeStore {

    private static final String FIELD_PERFORMANCE_ID = "performanceId";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_SEQUENCE = "sequence";
    private static final String FIELD_QUEUE_TOKEN = "queueToken";
    private static final String FIELD_EXPIRES_AT = "expiresAt";

    private final RedissonClient redissonClient;
    private final Clock clock;
    private final UuidSupplier uuidSupplier;

    @Override
    public long countActive(final Long performanceId) {
        return activeSet(performanceId).size();
    }

    @Override
    public QueueEntryRuntime admitNow(final Long performanceId, final Duration entryTokenTtl, final Duration entryRetention) {
        return admit(performanceId, uuidSupplier.get().toString(), null, entryTokenTtl, entryRetention);
    }

    @Override
    public QueueEntryRuntime enqueue(final Long performanceId, final Duration entryRetention) {
        final String queueEntryId = uuidSupplier.get().toString();
        final long sequence = sequence(performanceId).incrementAndGet();

        waitingSet(performanceId).add(sequence, queueEntryId);
        final RMap<String, String> entryMap = entryMap(queueEntryId);
        entryMap.put(FIELD_PERFORMANCE_ID, String.valueOf(performanceId));
        entryMap.put(FIELD_STATUS, QueueEntryStatus.WAITING.name());
        entryMap.put(FIELD_SEQUENCE, String.valueOf(sequence));
        entryMap.expire(entryRetention);

        return new QueueEntryRuntime(performanceId, queueEntryId, QueueEntryStatus.WAITING, sequence, null, null);
    }

    @Override
    public Optional<Long> findWaitingPosition(final Long performanceId, final String queueEntryId) {
        final Integer rank = waitingSet(performanceId).rank(queueEntryId);
        return rank == null ? Optional.empty() : Optional.of(rank.longValue() + 1L);
    }

    @Override
    public Optional<QueueEntryRuntime> findEntry(final String queueEntryId) {
        final Map<String, String> values = entryMap(queueEntryId).readAllMap();
        if (values.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new QueueEntryRuntime(
                Long.valueOf(values.get(FIELD_PERFORMANCE_ID)),
                queueEntryId,
                QueueEntryStatus.valueOf(values.get(FIELD_STATUS)),
                parseLong(values.get(FIELD_SEQUENCE)),
                values.get(FIELD_QUEUE_TOKEN),
                parseDateTime(values.get(FIELD_EXPIRES_AT))
        ));
    }

    @Override
    public boolean isValidToken(final Long performanceId, final String queueToken) {
        final QueueRedisKey.TokenKey tokenKey = QueueRedisKey.tryParseToken(queueToken).orElse(null);
        if (tokenKey == null || !performanceId.equals(tokenKey.performanceId())) {
            return false;
        }
        return tokenBucket(queueToken).get() != null;
    }

    @Override
    public Optional<QueueEntryRuntime> admitNextWaiting(final Long performanceId, final Duration entryTokenTtl, final Duration entryRetention) {
        while (true) {
            final String queueEntryId = waitingSet(performanceId).first();
            if (queueEntryId == null) {
                return Optional.empty();
            }
            waitingSet(performanceId).remove(queueEntryId);
            final QueueEntryRuntime entry = findEntry(queueEntryId).orElse(null);
            if (entry == null || entry.status() != QueueEntryStatus.WAITING) {
                continue;
            }
            return Optional.of(admit(performanceId, queueEntryId, entry.sequence(), entryTokenTtl, entryRetention));
        }
    }

    @Override
    public void expireAdmitted(final Long performanceId, final String queueEntryId, final String queueToken) {
        activeSet(performanceId).remove(queueToken);
        tokenBucket(queueToken).delete();
        updateEntry(queueEntryId, QueueEntryStatus.EXPIRED, null, null);
    }

    @Override
    public void leaveWaiting(final Long performanceId, final String queueEntryId) {
        waitingSet(performanceId).remove(queueEntryId);
        updateEntry(queueEntryId, QueueEntryStatus.LEFT, null, null);
    }

    @Override
    public void leaveAdmitted(final Long performanceId, final String queueEntryId, final String queueToken) {
        activeSet(performanceId).remove(queueToken);
        tokenBucket(queueToken).delete();
        updateEntry(queueEntryId, QueueEntryStatus.LEFT, null, null);
    }

    private QueueEntryRuntime admit(
            final Long performanceId,
            final String queueEntryId,
            final Long sequence,
            final Duration entryTokenTtl,
            final Duration entryRetention
    ) {
        final String queueToken = QueueRedisKey.createToken(performanceId, queueEntryId, uuidSupplier.get().toString());
        final LocalDateTime expiresAt = LocalDateTime.now(clock).plus(entryTokenTtl);

        tokenBucket(queueToken).set(queueEntryId, entryTokenTtl);
        activeSet(performanceId).add(queueToken);

        final RMap<String, String> entryMap = entryMap(queueEntryId);
        entryMap.put(FIELD_PERFORMANCE_ID, String.valueOf(performanceId));
        entryMap.put(FIELD_STATUS, QueueEntryStatus.ADMITTED.name());
        if (sequence != null) {
            entryMap.put(FIELD_SEQUENCE, String.valueOf(sequence));
        }
        entryMap.put(FIELD_QUEUE_TOKEN, queueToken);
        entryMap.put(FIELD_EXPIRES_AT, expiresAt.toString());
        entryMap.expire(entryRetention);

        return new QueueEntryRuntime(performanceId, queueEntryId, QueueEntryStatus.ADMITTED, sequence, queueToken, expiresAt);
    }

    private void updateEntry(
            final String queueEntryId,
            final QueueEntryStatus status,
            final String queueToken,
            final LocalDateTime expiresAt
    ) {
        final RMap<String, String> entryMap = entryMap(queueEntryId);
        if (entryMap.isEmpty()) {
            return;
        }
        entryMap.put(FIELD_STATUS, status.name());
        if (queueToken == null) {
            entryMap.remove(FIELD_QUEUE_TOKEN);
        } else {
            entryMap.put(FIELD_QUEUE_TOKEN, queueToken);
        }
        if (expiresAt == null) {
            entryMap.remove(FIELD_EXPIRES_AT);
        } else {
            entryMap.put(FIELD_EXPIRES_AT, expiresAt.toString());
        }
    }

    private RScoredSortedSet<String> waitingSet(final Long performanceId) {
        return redissonClient.getScoredSortedSet(QueueRedisKey.waiting(performanceId), StringCodec.INSTANCE);
    }

    private RSet<String> activeSet(final Long performanceId) {
        return redissonClient.getSet(QueueRedisKey.active(performanceId), StringCodec.INSTANCE);
    }

    private RAtomicLong sequence(final Long performanceId) {
        return redissonClient.getAtomicLong(QueueRedisKey.sequence(performanceId));
    }

    private RMap<String, String> entryMap(final String queueEntryId) {
        return redissonClient.getMap(QueueRedisKey.entry(queueEntryId), StringCodec.INSTANCE);
    }

    private RBucket<String> tokenBucket(final String queueToken) {
        return redissonClient.getBucket(QueueRedisKey.tokenStorageKey(queueToken), StringCodec.INSTANCE);
    }

    private Long parseLong(final String value) {
        return value == null ? null : Long.valueOf(value);
    }

    private LocalDateTime parseDateTime(final String value) {
        return value == null ? null : LocalDateTime.parse(value);
    }
}
