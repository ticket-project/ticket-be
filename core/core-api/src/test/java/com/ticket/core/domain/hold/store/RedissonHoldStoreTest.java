package com.ticket.core.domain.hold.store;

import com.ticket.core.domain.hold.model.HoldSnapshot;
import com.ticket.core.domain.hold.support.HoldSnapshotCodec;
import com.ticket.core.domain.performanceseat.support.SeatRedisKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RBucket;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class RedissonHoldStoreTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private HoldSnapshotCodec holdSnapshotCodec;

    @InjectMocks
    private RedissonHoldStore redissonHoldStore;

    @Test
    void 홀드를_저장하면_좌석키와_메타키를_저장한다() {
        //given
        Duration ttl = Duration.ofMinutes(5);
        HoldSnapshot snapshot = new HoldSnapshot("hold-key", 7L, 1L, List.of(10L, 20L), LocalDateTime.of(2026, 3, 15, 19, 5));
        RBucket<Object> seat10 = mock(RBucket.class);
        RBucket<Object> seat20 = mock(RBucket.class);
        RBucket<Object> meta = mock(RBucket.class);

        when(redissonClient.getBucket(SeatRedisKey.hold(1L, 10L), StringCodec.INSTANCE)).thenReturn(seat10);
        when(redissonClient.getBucket(SeatRedisKey.hold(1L, 20L), StringCodec.INSTANCE)).thenReturn(seat20);
        when(redissonClient.getBucket(SeatRedisKey.holdMeta("hold-key"), StringCodec.INSTANCE)).thenReturn(meta);
        when(holdSnapshotCodec.encode(any(HoldSnapshot.class))).thenReturn("payload");

        //when
        redissonHoldStore.save(snapshot, ttl);

        //then
        verify(seat10).set("hold-key", ttl);
        verify(seat20).set("hold-key", ttl);
        verify(meta).set("payload", ttl);
    }

    @Test
    void 홀드저장_중_예외가_나면_생성한_좌석키를_롤백한다() {
        //given
        Duration ttl = Duration.ofMinutes(5);
        HoldSnapshot snapshot = new HoldSnapshot("hold-key", 7L, 1L, List.of(10L, 20L), LocalDateTime.of(2026, 3, 15, 19, 5));
        RBucket<Object> seat10 = mock(RBucket.class);
        RBucket<Object> seat20 = mock(RBucket.class);
        RBucket<Object> meta = mock(RBucket.class);

        when(redissonClient.getBucket(SeatRedisKey.hold(1L, 10L), StringCodec.INSTANCE)).thenReturn(seat10);
        when(redissonClient.getBucket(SeatRedisKey.hold(1L, 20L), StringCodec.INSTANCE)).thenReturn(seat20);
        when(redissonClient.getBucket(SeatRedisKey.holdMeta("hold-key"), StringCodec.INSTANCE)).thenReturn(meta);
        doThrow(new RuntimeException("boom")).when(seat20).set("hold-key", ttl);

        //when
        //then
        assertThatThrownBy(() -> redissonHoldStore.save(snapshot, ttl))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hold Redis");

        verify(seat10).delete();
        verify(meta).delete();
    }

    @Test
    void release는_같은_holdKey만_삭제한다() {
        //given
        RBucket<Object> seat10 = bucketReturning("hold-key");
        RBucket<Object> seat20 = bucketReturning("other-hold");
        RBucket<Object> meta = mock(RBucket.class);

        when(redissonClient.getBucket(SeatRedisKey.hold(1L, 10L), StringCodec.INSTANCE)).thenReturn(seat10);
        when(redissonClient.getBucket(SeatRedisKey.hold(1L, 20L), StringCodec.INSTANCE)).thenReturn(seat20);
        when(redissonClient.getBucket(SeatRedisKey.holdMeta("hold-key"), StringCodec.INSTANCE)).thenReturn(meta);

        //when
        redissonHoldStore.release(1L, "hold-key", List.of(20L, 10L, 10L));

        //then
        verify(seat10).delete();
        verify(meta).delete();
    }

    @Test
    void 현재_hold중인_좌석아이디들을_조회한다() {
        //given
        RKeys keys = mock(RKeys.class);
        when(redissonClient.getKeys()).thenReturn(keys);
        when(keys.getKeysByPattern(SeatRedisKey.holdPattern(1L)))
                .thenReturn(List.of(SeatRedisKey.hold(1L, 30L), SeatRedisKey.hold(1L, 10L)));

        //when
        Set<Long> result = redissonHoldStore.getHoldingSeatIds(1L);

        //then
        assertThat(result).containsExactlyInAnyOrder(10L, 30L);
    }

    @Test
    void isHeld는_bucket값_존재여부를_반환한다() {
        //given
        RBucket<Object> seatBucket = bucketReturning("hold-key");
        when(redissonClient.getBucket(SeatRedisKey.hold(1L, 10L), StringCodec.INSTANCE)).thenReturn(seatBucket);

        //when
        boolean result = redissonHoldStore.isHeld(1L, 10L);

        //then
        assertThat(result).isTrue();
    }

    @SuppressWarnings("unchecked")
    private RBucket<Object> bucketReturning(final String value) {
        RBucket<Object> bucket = mock(RBucket.class);
        when(bucket.get()).thenReturn(value);
        return bucket;
    }
}
