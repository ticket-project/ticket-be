package com.ticket.core.infra.queue;

import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.domain.queue.model.QueueEntryId;
import com.ticket.core.domain.queue.runtime.QueueJoinResult;
import com.ticket.core.domain.queue.runtime.QueueRedisKey;
import com.ticket.core.domain.queue.runtime.QueueTicket;
import com.ticket.core.domain.queue.runtime.QueueTicketStore;
import com.ticket.core.domain.queue.support.QueuePolicy;
import com.ticket.core.support.random.UuidSupplier;
import lombok.RequiredArgsConstructor;
import org.redisson.api.*;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisQueueTicketStore implements QueueTicketStore {

    private static final String ENTER_SCRIPT = """
            local waitingKey = KEYS[1]
            local activeKey = KEYS[2]
            local sequenceKey = KEYS[3]
            local memberKey = KEYS[4]

            local performanceId = ARGV[1]
            local memberId = ARGV[2]
            local queueEntryId = ARGV[3]
            local tokenId = ARGV[4]
            local advanceTokenId = ARGV[5]
            local enabled = ARGV[6]
            local maxActiveUsers = tonumber(ARGV[7])
            local entryTokenTtlMillis = tonumber(ARGV[8])
            local entryRetentionMillis = tonumber(ARGV[9])
            local expiresAt = ARGV[10]
            local entryPrefix = ARGV[11]
            local tokenPrefix = ARGV[12]
            local memberEntryPrefix = ARGV[13]

            local function to_map(values)
                local map = {}
                for i = 1, #values, 2 do
                    map[values[i]] = values[i + 1]
                end
                return map
            end

            local function clear_member_if_current(entryId)
                if redis.call('GET', memberKey) == entryId then
                    redis.call('DEL', memberKey)
                end
            end

            local function mark_left(entryKey)
                redis.call('HSET', entryKey, 'status', 'LEFT')
                redis.call('HDEL', entryKey, 'queueToken', 'expiresAt')
            end

            local function admit_waiting(entryId, admitTokenId)
                local entryKey = entryPrefix .. entryId
                local values = redis.call('HGETALL', entryKey)
                if #values == 0 then
                    return false
                end

                local entry = to_map(values)
                if entry['status'] ~= 'WAITING' then
                    return false
                end

                local admittedMemberId = entry['memberId']
                local queueToken = performanceId .. ':' .. entryId .. ':' .. admitTokenId
                redis.call('SET', tokenPrefix .. queueToken, entryId, 'PX', entryTokenTtlMillis)
                redis.call('SADD', activeKey, queueToken)
                redis.call(
                    'HSET',
                    entryKey,
                    'performanceId', performanceId,
                    'memberId', admittedMemberId,
                    'status', 'ADMITTED',
                    'queueToken', queueToken,
                    'expiresAt', expiresAt
                )
                redis.call('PEXPIRE', entryKey, entryRetentionMillis)
                if admittedMemberId ~= false and admittedMemberId ~= nil then
                    redis.call('SET', memberEntryPrefix .. admittedMemberId, entryId, 'PX', entryRetentionMillis)
                end
                return true
            end

            local function advance_one()
                if redis.call('SCARD', activeKey) >= maxActiveUsers then
                    return
                end

                local candidates = redis.call('ZCARD', waitingKey)
                for i = 1, candidates do
                    local nextEntries = redis.call('ZRANGE', waitingKey, 0, 0)
                    local nextEntryId = nextEntries[1]
                    if nextEntryId == nil then
                        return
                    end

                    redis.call('ZREM', waitingKey, nextEntryId)
                    if admit_waiting(nextEntryId, advanceTokenId) then
                        return
                    end
                end
            end

            local existingEntryId = redis.call('GET', memberKey)
            if existingEntryId ~= false and existingEntryId ~= nil then
                local existingEntryKey = entryPrefix .. existingEntryId
                local values = redis.call('HGETALL', existingEntryKey)
                if #values == 0 then
                    redis.call('DEL', memberKey)
                else
                    local existingEntry = to_map(values)
                    if existingEntry['performanceId'] == performanceId and existingEntry['memberId'] == memberId then
                        if existingEntry['status'] == 'WAITING' then
                            redis.call('ZREM', waitingKey, existingEntryId)
                            mark_left(existingEntryKey)
                            clear_member_if_current(existingEntryId)
                        elseif existingEntry['status'] == 'ADMITTED' then
                            local existingToken = existingEntry['queueToken']
                            if existingToken ~= false and existingToken ~= nil then
                                redis.call('SREM', activeKey, existingToken)
                                redis.call('DEL', tokenPrefix .. existingToken)
                            end
                            mark_left(existingEntryKey)
                            clear_member_if_current(existingEntryId)
                            advance_one()
                        else
                            redis.call('DEL', memberKey)
                        end
                    else
                        redis.call('DEL', memberKey)
                    end
                end
            end

            local activeUsers = redis.call('SCARD', activeKey)
            local waitingUsers = redis.call('ZCARD', waitingKey)
            if (enabled == '0' or activeUsers < maxActiveUsers) and waitingUsers == 0 then
                local queueToken = performanceId .. ':' .. queueEntryId .. ':' .. tokenId
                redis.call('SET', tokenPrefix .. queueToken, queueEntryId, 'PX', entryTokenTtlMillis)
                redis.call('SADD', activeKey, queueToken)
                local entryKey = entryPrefix .. queueEntryId
                redis.call(
                    'HSET',
                    entryKey,
                    'performanceId', performanceId,
                    'memberId', memberId,
                    'status', 'ADMITTED',
                    'queueToken', queueToken,
                    'expiresAt', expiresAt
                )
                redis.call('PEXPIRE', entryKey, entryRetentionMillis)
                redis.call('SET', memberKey, queueEntryId, 'PX', entryRetentionMillis)
                return {'ADMITTED', queueEntryId, '', queueToken, expiresAt}
            end

            local sequence = redis.call('INCR', sequenceKey)
            local position = waitingUsers + 1
            redis.call('ZADD', waitingKey, sequence, queueEntryId)
            local entryKey = entryPrefix .. queueEntryId
            redis.call(
                'HSET',
                entryKey,
                'performanceId', performanceId,
                'memberId', memberId,
                'status', 'WAITING',
                'sequence', tostring(sequence)
            )
            redis.call('PEXPIRE', entryKey, entryRetentionMillis)
            redis.call('SET', memberKey, queueEntryId, 'PX', entryRetentionMillis)
            return {'WAITING', queueEntryId, tostring(position), '', ''}
            """;

    private static final String FIELD_PERFORMANCE_ID = "performanceId";
    private static final String FIELD_MEMBER_ID = "memberId";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_SEQUENCE = "sequence";
    private static final String FIELD_QUEUE_TOKEN = "queueToken";
    private static final String FIELD_EXPIRES_AT = "expiresAt";

    private final RedissonClient redissonClient;
    private final UuidSupplier uuidSupplier;

    @Override
    public QueueJoinResult enter(
            final Long performanceId,
            final Long memberId,
            final QueuePolicy policy,
            final LocalDateTime now
    ) {
        final String queueEntryId = uuidSupplier.get().toString();
        final String tokenId = uuidSupplier.get().toString();
        final String advanceTokenId = uuidSupplier.get().toString();
        final LocalDateTime expiresAt = now.plus(policy.entryTokenTtl());

        final List<Object> keys = List.of(
                QueueRedisKey.waiting(performanceId),
                QueueRedisKey.active(performanceId),
                QueueRedisKey.sequence(performanceId),
                QueueRedisKey.memberEntry(performanceId, memberId)
        );
        final Object[] arguments = {
                String.valueOf(performanceId),
                String.valueOf(memberId),
                queueEntryId,
                tokenId,
                advanceTokenId,
                policy.enabled() ? "1" : "0",
                String.valueOf(policy.maxActiveUsers()),
                String.valueOf(policy.entryTokenTtl().toMillis()),
                String.valueOf(policy.entryRetention().toMillis()),
                expiresAt.toString(),
                QueueRedisKey.entryPrefix(),
                QueueRedisKey.tokenPrefix(),
                QueueRedisKey.memberEntryPrefix(performanceId)
        };

        final List<Object> result = redissonClient.getScript(StringCodec.INSTANCE).eval(
                RScript.Mode.READ_WRITE,
                ENTER_SCRIPT,
                RScript.ReturnType.LIST,
                keys,
                arguments
        );
        return toJoinResult(result);
    }

    @Override
    public long countActive(final Long performanceId) {
        return activeSet(performanceId).size();
    }

    @Override
    public long countWaiting(final Long performanceId) {
        return waitingSet(performanceId).size();
    }

    @Override
    public QueueTicket admitNow(
            final Long performanceId,
            final Long memberId,
            final Duration entryTokenTtl,
            final Duration entryRetention,
            final LocalDateTime now
    ) {
        return admit(performanceId, memberId, uuidSupplier.get().toString(), null, entryTokenTtl, entryRetention, now);
    }

    @Override
    public QueueTicket enqueue(final Long performanceId, final Long memberId, final Duration entryRetention) {
        final String queueEntryId = uuidSupplier.get().toString();
        final long sequence = sequence(performanceId).incrementAndGet();

        waitingSet(performanceId).add(sequence, queueEntryId);
        final RMap<String, String> entryMap = entryMap(queueEntryId);
        entryMap.put(FIELD_PERFORMANCE_ID, String.valueOf(performanceId));
        entryMap.put(FIELD_MEMBER_ID, String.valueOf(memberId));
        entryMap.put(FIELD_STATUS, QueueEntryStatus.WAITING.name());
        entryMap.put(FIELD_SEQUENCE, String.valueOf(sequence));
        entryMap.expire(entryRetention);
        memberEntryBucket(performanceId, memberId).set(queueEntryId, entryRetention);

        return new QueueTicket(performanceId, memberId, queueEntryId, QueueEntryStatus.WAITING, sequence, null, null);
    }

    @Override
    public Optional<String> findMemberEntryId(final Long performanceId, final Long memberId) {
        return Optional.ofNullable(memberEntryBucket(performanceId, memberId).get());
    }

    @Override
    public void clearMemberEntry(final Long performanceId, final Long memberId) {
        memberEntryBucket(performanceId, memberId).delete();
    }

    @Override
    public Optional<Long> findWaitingPosition(final Long performanceId, final QueueEntryId queueEntryId) {
        final Integer rank = waitingSet(performanceId).rank(queueEntryId.value());
        return rank == null ? Optional.empty() : Optional.of(rank.longValue() + 1L);
    }

    @Override
    public Optional<QueueTicket> findEntry(final QueueEntryId queueEntryId) {
        final Map<String, String> values = entryMap(queueEntryId.value()).readAllMap();
        if (values.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new QueueTicket(
                Long.valueOf(values.get(FIELD_PERFORMANCE_ID)),
                parseLong(values.get(FIELD_MEMBER_ID)),
                queueEntryId.value(),
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
    public Optional<QueueTicket> admitNextWaiting(
            final Long performanceId,
            final Duration entryTokenTtl,
            final Duration entryRetention,
            final LocalDateTime now
    ) {
        final RScoredSortedSet<String> waitingSet = waitingSet(performanceId);
        final int candidates = waitingSet.size();
        for (int i = 0; i < candidates; i++) {
            final String queueEntryId = waitingSet.first();
            if (queueEntryId == null) {
                return Optional.empty();
            }
            waitingSet.remove(queueEntryId);
            final QueueTicket entry = findEntry(QueueEntryId.from(queueEntryId)).orElse(null);
            if (entry == null || entry.status() != QueueEntryStatus.WAITING) {
                continue;
            }
            return Optional.of(admit(
                    performanceId,
                    entry.memberId(),
                    queueEntryId,
                    entry.sequence(),
                    entryTokenTtl,
                    entryRetention,
                    now
            ));
        }
        return Optional.empty();
    }

    @Override
    public void expireAdmitted(final Long performanceId, final QueueEntryId queueEntryId, final String queueToken) {
        final QueueTicket entry = findEntry(queueEntryId).orElse(null);
        activeSet(performanceId).remove(queueToken);
        tokenBucket(queueToken).delete();
        updateEntry(queueEntryId.value(), QueueEntryStatus.EXPIRED, null, null);
        clearMemberEntryIfMatches(entry, performanceId, queueEntryId.value());
    }

    @Override
    public void leaveWaiting(final Long performanceId, final QueueEntryId queueEntryId) {
        final QueueTicket entry = findEntry(queueEntryId).orElse(null);
        waitingSet(performanceId).remove(queueEntryId.value());
        updateEntry(queueEntryId.value(), QueueEntryStatus.LEFT, null, null);
        clearMemberEntryIfMatches(entry, performanceId, queueEntryId.value());
    }

    @Override
    public void leaveAdmitted(final Long performanceId, final QueueEntryId queueEntryId, final String queueToken) {
        final QueueTicket entry = findEntry(queueEntryId).orElse(null);
        activeSet(performanceId).remove(queueToken);
        tokenBucket(queueToken).delete();
        updateEntry(queueEntryId.value(), QueueEntryStatus.LEFT, null, null);
        clearMemberEntryIfMatches(entry, performanceId, queueEntryId.value());
    }

    private QueueTicket admit(
            final Long performanceId,
            final Long memberId,
            final String queueEntryId,
            final Long sequence,
            final Duration entryTokenTtl,
            final Duration entryRetention,
            final LocalDateTime now
    ) {
        final String queueToken = QueueRedisKey.createToken(performanceId, queueEntryId, uuidSupplier.get().toString());
        final LocalDateTime expiresAt = now.plus(entryTokenTtl);

        tokenBucket(queueToken).set(queueEntryId, entryTokenTtl);
        activeSet(performanceId).add(queueToken);

        final RMap<String, String> entryMap = entryMap(queueEntryId);
        entryMap.put(FIELD_PERFORMANCE_ID, String.valueOf(performanceId));
        entryMap.put(FIELD_MEMBER_ID, String.valueOf(memberId));
        entryMap.put(FIELD_STATUS, QueueEntryStatus.ADMITTED.name());
        if (sequence != null) {
            entryMap.put(FIELD_SEQUENCE, String.valueOf(sequence));
        }
        entryMap.put(FIELD_QUEUE_TOKEN, queueToken);
        entryMap.put(FIELD_EXPIRES_AT, expiresAt.toString());
        entryMap.expire(entryRetention);
        memberEntryBucket(performanceId, memberId).set(queueEntryId, entryRetention);

        return new QueueTicket(performanceId, memberId, queueEntryId, QueueEntryStatus.ADMITTED, sequence, queueToken, expiresAt);
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

    private RBucket<String> memberEntryBucket(final Long performanceId, final Long memberId) {
        return redissonClient.getBucket(QueueRedisKey.memberEntry(performanceId, memberId), StringCodec.INSTANCE);
    }

    private void clearMemberEntryIfMatches(
            final QueueTicket entry,
            final Long performanceId,
            final String queueEntryId
    ) {
        if (entry == null || entry.memberId() == null || !performanceId.equals(entry.performanceId())) {
            return;
        }
        final RBucket<String> bucket = memberEntryBucket(performanceId, entry.memberId());
        final String currentEntryId = bucket.get();
        if (queueEntryId.equals(currentEntryId)) {
            bucket.delete();
        }
    }

    private QueueJoinResult toJoinResult(final List<Object> values) {
        return new QueueJoinResult(
                QueueEntryStatus.valueOf(requiredString(values, 0)),
                requiredString(values, 1),
                parseLong(emptyToNull(valueAt(values, 2))),
                emptyToNull(valueAt(values, 3)),
                parseDateTime(emptyToNull(valueAt(values, 4)))
        );
    }

    private String requiredString(final List<Object> values, final int index) {
        final String value = valueAt(values, index);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Redis queue enter script returned blank value at index " + index);
        }
        return value;
    }

    private String valueAt(final List<Object> values, final int index) {
        if (values == null || values.size() <= index || values.get(index) == null) {
            return null;
        }
        return String.valueOf(values.get(index));
    }

    private String emptyToNull(final String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private Long parseLong(final String value) {
        return value == null ? null : Long.valueOf(value);
    }

    private LocalDateTime parseDateTime(final String value) {
        return value == null ? null : LocalDateTime.parse(value);
    }
}
