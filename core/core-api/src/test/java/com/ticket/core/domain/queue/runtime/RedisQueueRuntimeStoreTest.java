package com.ticket.core.domain.queue.runtime;

import com.ticket.core.domain.queue.model.QueueEntryStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class RedisQueueRuntimeStoreTest {

    @Mock
    private RedissonClient redissonClient;

    @InjectMocks
    private RedisQueueRuntimeStore redisQueueRuntimeStore;

    @Test
    void active_개수를_반환한다() {
        //given
        @SuppressWarnings("unchecked")
        RSet<Object> activeSet = mock(RSet.class);
        when(redissonClient.getSet(QueueRedisKey.active(10L), StringCodec.INSTANCE)).thenReturn(activeSet);
        when(activeSet.size()).thenReturn(3);

        //when
        long result = redisQueueRuntimeStore.countActive(10L);

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
        Optional<QueueEntryRuntime> result = redisQueueRuntimeStore.findEntry("qe-10");

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
                "status", QueueEntryStatus.ADMITTED.name(),
                "sequence", "5",
                "queueToken", "10:qe-10:token",
                "expiresAt", "2026-03-15T20:30:00"
        ));

        //when
        QueueEntryRuntime result = redisQueueRuntimeStore.findEntry("qe-10").orElseThrow();

        //then
        assertThat(result.performanceId()).isEqualTo(10L);
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
        assertThat(redisQueueRuntimeStore.isValidToken(10L, "broken-token")).isFalse();
    }

    @Test
    void 다른_공연의_토큰이면_isValidToken은_false다() {
        //given
        //when
        String token = QueueRedisKey.createToken(20L, "qe-20");

        //then
        assertThat(redisQueueRuntimeStore.isValidToken(10L, token)).isFalse();
    }

    @Test
    void 저장된_토큰이_있으면_isValidToken은_true다() {
        //given
        String token = QueueRedisKey.createToken(10L, "qe-10");
        @SuppressWarnings("unchecked")
        RBucket<Object> bucket = mock(RBucket.class);
        when(redissonClient.getBucket(QueueRedisKey.tokenStorageKey(token), StringCodec.INSTANCE)).thenReturn(bucket);
        when(bucket.get()).thenReturn("qe-10");

        //when
        boolean result = redisQueueRuntimeStore.isValidToken(10L, token);

        //then
        assertThat(result).isTrue();
    }
}

