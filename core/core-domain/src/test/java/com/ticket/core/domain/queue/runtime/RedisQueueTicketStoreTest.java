package com.ticket.core.domain.queue.runtime;

import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.domain.queue.model.QueueEntryId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RSet;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class RedisQueueTicketStoreTest {

    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2026, 3, 15, 19, 0);

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private com.ticket.core.support.random.UuidSupplier uuidSupplier;

    private RedisQueueTicketStore redisQueueTicketStore;

    @BeforeEach
    void setUp() {
        this.redisQueueTicketStore = new RedisQueueTicketStore(redissonClient, uuidSupplier);
    }

    @Test
    void active_개수를_반환한다() {
        //given
        @SuppressWarnings("unchecked")
        RSet<Object> activeSet = mock(RSet.class);
        when(redissonClient.getSet(QueueRedisKey.active(10L), StringCodec.INSTANCE)).thenReturn(activeSet);
        when(activeSet.size()).thenReturn(3);

        //when
        long result = redisQueueTicketStore.countActive(10L);

        //then
        assertThat(result).isEqualTo(3L);
    }

    @Test
    void 엔트리맵이_비어있으면_findEntry는_empty다() {
        //given
        @SuppressWarnings("unchecked")
        RMap<Object, Object> entryMap = mock(RMap.class);
        when(redissonClient.getMap(QueueRedisKey.entry("qe-10"), StringCodec.INSTANCE)).thenReturn(entryMap);
        when(entryMap.readAllMap()).thenReturn(Map.of());

        //when
        Optional<QueueTicket> result = redisQueueTicketStore.findEntry(QueueEntryId.from("qe-10"));

        //then
        assertThat(result).isEmpty();
    }

    @Test
    void 엔트리맵이_있으면_runtime으로_복원한다() {
        //given
        @SuppressWarnings("unchecked")
        RMap<Object, Object> entryMap = mock(RMap.class);
        when(redissonClient.getMap(QueueRedisKey.entry("qe-10"), StringCodec.INSTANCE)).thenReturn(entryMap);
        when(entryMap.readAllMap()).thenReturn(Map.of(
                "performanceId", "10",
                "memberId", "100",
                "status", QueueEntryStatus.ADMITTED.name(),
                "sequence", "5",
                "queueToken", "10:qe-10:token",
                "expiresAt", "2026-03-15T20:30:00"
        ));

        //when
        QueueTicket result = redisQueueTicketStore.findEntry(QueueEntryId.from("qe-10")).orElseThrow();

        //then
        assertThat(result.performanceId()).isEqualTo(10L);
        assertThat(result.memberId()).isEqualTo(100L);
        assertThat(result.queueEntryId()).isEqualTo("qe-10");
        assertThat(result.status()).isEqualTo(QueueEntryStatus.ADMITTED);
        assertThat(result.sequence()).isEqualTo(5L);
        assertThat(result.queueToken()).isEqualTo("10:qe-10:token");
        assertThat(result.expiresAt()).isEqualTo(LocalDateTime.of(2026, 3, 15, 20, 30));
    }

    @Test
    void 잘못된_토큰이면_isValidToken은_false다() {
        //given
        //when
        //then
        assertThat(redisQueueTicketStore.isValidToken(10L, "broken-token")).isFalse();
    }

    @Test
    void 다른_공연의_토큰이면_isValidToken은_false다() {
        //given
        //when
        String token = QueueRedisKey.createToken(20L, "qe-20", "token");

        //then
        assertThat(redisQueueTicketStore.isValidToken(10L, token)).isFalse();
    }

    @Test
    void 저장된_토큰이_있으면_isValidToken은_true다() {
        //given
        String token = QueueRedisKey.createToken(10L, "qe-10", "token");
        @SuppressWarnings("unchecked")
        RBucket<Object> bucket = mock(RBucket.class);
        when(redissonClient.getBucket(QueueRedisKey.tokenStorageKey(token), StringCodec.INSTANCE)).thenReturn(bucket);
        when(bucket.get()).thenReturn("qe-10");

        //when
        boolean result = redisQueueTicketStore.isValidToken(10L, token);

        //then
        assertThat(result).isTrue();
    }

    @Test
    void admitNow는_고정된_uuid와_시각으로_토큰과_만료시각을_생성한다() {
        //given
        @SuppressWarnings("unchecked")
        RBucket<Object> tokenBucket = mock(RBucket.class);
        @SuppressWarnings("unchecked")
        RSet<Object> activeSet = mock(RSet.class);
        @SuppressWarnings("unchecked")
        RMap<Object, Object> entryMap = mock(RMap.class);
        @SuppressWarnings("unchecked")
        RBucket<Object> memberEntryBucket = mock(RBucket.class);
        when(uuidSupplier.get())
                .thenReturn(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .thenReturn(UUID.fromString("123e4567-e89b-12d3-a456-426614174001"));
        when(redissonClient.getBucket("queue:token:10:123e4567-e89b-12d3-a456-426614174000:123e4567-e89b-12d3-a456-426614174001", StringCodec.INSTANCE))
                .thenReturn(tokenBucket);
        when(redissonClient.getBucket("queue:performance:10:member:200", StringCodec.INSTANCE))
                .thenReturn(memberEntryBucket);
        when(redissonClient.getSet(QueueRedisKey.active(10L), StringCodec.INSTANCE)).thenReturn(activeSet);
        when(redissonClient.getMap(QueueRedisKey.entry("123e4567-e89b-12d3-a456-426614174000"), StringCodec.INSTANCE)).thenReturn(entryMap);

        //when
        QueueTicket result = redisQueueTicketStore.admitNow(10L, 200L, Duration.ofMinutes(3), Duration.ofMinutes(10), FIXED_NOW);

        //then
        assertThat(result.memberId()).isEqualTo(200L);
        assertThat(result.queueEntryId()).isEqualTo("123e4567-e89b-12d3-a456-426614174000");
        assertThat(result.queueToken()).isEqualTo("10:123e4567-e89b-12d3-a456-426614174000:123e4567-e89b-12d3-a456-426614174001");
        assertThat(result.expiresAt()).isEqualTo(LocalDateTime.of(2026, 3, 15, 19, 3));
        verify(tokenBucket).set("123e4567-e89b-12d3-a456-426614174000", Duration.ofMinutes(3));
        verify(activeSet).add("10:123e4567-e89b-12d3-a456-426614174000:123e4567-e89b-12d3-a456-426614174001");
        verify(memberEntryBucket).set("123e4567-e89b-12d3-a456-426614174000", Duration.ofMinutes(10));
    }

    @Test
    void 회원과_공연으로_현재_엔트리를_조회한다() {
        //given
        @SuppressWarnings("unchecked")
        RBucket<Object> memberEntryBucket = mock(RBucket.class);
        when(redissonClient.getBucket("queue:performance:10:member:200", StringCodec.INSTANCE)).thenReturn(memberEntryBucket);
        when(memberEntryBucket.get()).thenReturn("qe-200");

        //when
        Optional<String> result = redisQueueTicketStore.findMemberEntryId(10L, 200L);

        //then
        assertThat(result).contains("qe-200");
    }

    @Test
    void admitNextWaiting은_현재_대기열_크기만큼만_오염엔트리를_정리하고_종료한다() {
        @SuppressWarnings("unchecked")
        RScoredSortedSet<Object> waitingSet = mock(RScoredSortedSet.class);
        @SuppressWarnings("unchecked")
        RMap<Object, Object> entryMap1 = mock(RMap.class);
        @SuppressWarnings("unchecked")
        RMap<Object, Object> entryMap2 = mock(RMap.class);

        when(redissonClient.getScoredSortedSet(QueueRedisKey.waiting(10L), StringCodec.INSTANCE)).thenReturn(waitingSet);
        when(waitingSet.size()).thenReturn(2);
        when(waitingSet.first()).thenReturn("qe-1", "qe-2");
        when(redissonClient.getMap(QueueRedisKey.entry("qe-1"), StringCodec.INSTANCE)).thenReturn(entryMap1);
        when(redissonClient.getMap(QueueRedisKey.entry("qe-2"), StringCodec.INSTANCE)).thenReturn(entryMap2);
        when(entryMap1.readAllMap()).thenReturn(Map.of());
        when(entryMap2.readAllMap()).thenReturn(Map.of());

        Optional<QueueTicket> result = redisQueueTicketStore.admitNextWaiting(10L, Duration.ofMinutes(3), Duration.ofMinutes(10), FIXED_NOW);

        assertThat(result).isEmpty();
        verify(waitingSet).size();
        verify(waitingSet, times(2)).first();
        verify(waitingSet).remove("qe-1");
        verify(waitingSet).remove("qe-2");
    }
}
