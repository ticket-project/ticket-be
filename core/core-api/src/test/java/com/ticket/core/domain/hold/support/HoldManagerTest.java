package com.ticket.core.domain.hold.support;

import com.ticket.core.domain.hold.application.HoldKeyGenerator;
import com.ticket.core.domain.hold.model.HoldSnapshot;
import com.ticket.core.domain.performanceseat.support.SeatRedisKey;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RBucket;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class HoldManagerTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-03-15T10:00:00Z"), ZoneId.of("Asia/Seoul"));

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private HoldSnapshotCodec holdSnapshotCodec;

    @Mock
    private HoldKeyGenerator holdKeyGenerator;

    private HoldManager holdManager;

    @BeforeEach
    void setUp() {
        this.holdManager = new HoldManager(redissonClient, holdSnapshotCodec, holdKeyGenerator, FIXED_CLOCK);
    }

    @Test
    void 이미_hold된_좌석이_있으면_SEAT_ALREADY_HOLD_예외를_던진다() {
        //given
        RBucket<Object> seatBucket = bucketReturning("other-hold");
        when(holdKeyGenerator.generate()).thenReturn("hold-key");
        when(redissonClient.getBucket(SeatRedisKey.hold(1L, 10L), StringCodec.INSTANCE)).thenReturn(seatBucket);

        //when
        //then
        assertThatThrownBy(() -> holdManager.createHold(1L, 1L, List.of(10L), Duration.ofMinutes(5)))
                .isInstanceOf(CoreException.class)
                .satisfies(thrown -> assertThat(((CoreException) thrown).getErrorType()).isEqualTo(ErrorType.SEAT_ALREADY_HOLD));
    }

    @Test
    void hold를_생성하면_좌석키와_메타키를_저장하고_snapshot을_반환한다() {
        //given
        Duration ttl = Duration.ofMinutes(5);
        RBucket<Object> seat10 = bucketReturning(null);
        RBucket<Object> seat20 = bucketReturning(null);
        RBucket<Object> meta = mock(RBucket.class);

        when(holdKeyGenerator.generate()).thenReturn("hold-key");
        when(redissonClient.getBucket(SeatRedisKey.hold(1L, 10L), StringCodec.INSTANCE)).thenReturn(seat10);
        when(redissonClient.getBucket(SeatRedisKey.hold(1L, 20L), StringCodec.INSTANCE)).thenReturn(seat20);
        when(redissonClient.getBucket(SeatRedisKey.holdMeta("hold-key"), StringCodec.INSTANCE)).thenReturn(meta);
        when(holdSnapshotCodec.encode(any(HoldSnapshot.class))).thenReturn("payload");

        //when
        HoldSnapshot snapshot = holdManager.createHold(7L, 1L, List.of(10L, 20L), ttl);

        //then
        assertThat(snapshot.holdKey()).isEqualTo("hold-key");
        assertThat(snapshot.memberId()).isEqualTo(7L);
        assertThat(snapshot.performanceId()).isEqualTo(1L);
        assertThat(snapshot.seatIds()).containsExactly(10L, 20L);
        assertThat(snapshot.expiresAt()).isEqualTo(LocalDateTime.of(2026, 3, 15, 19, 5));
        verify(seat10).set("hold-key", ttl);
        verify(seat20).set("hold-key", ttl);
        verify(meta).set("payload", ttl);
    }

    @Test
    void hold저장_중_예외가_나면_생성한_좌석키를_롤백하고_IllegalStateException을_던진다() {
        //given
        Duration ttl = Duration.ofMinutes(5);
        RBucket<Object> seat10 = bucketReturning(null);
        RBucket<Object> seat20 = bucketReturning(null);
        RBucket<Object> meta = mock(RBucket.class);

        when(holdKeyGenerator.generate()).thenReturn("hold-key");
        when(redissonClient.getBucket(SeatRedisKey.hold(1L, 10L), StringCodec.INSTANCE)).thenReturn(seat10);
        when(redissonClient.getBucket(SeatRedisKey.hold(1L, 20L), StringCodec.INSTANCE)).thenReturn(seat20);
        when(redissonClient.getBucket(SeatRedisKey.holdMeta("hold-key"), StringCodec.INSTANCE)).thenReturn(meta);
        doThrow(new RuntimeException("boom")).when(seat20).set("hold-key", ttl);

        //when
        //then
        assertThatThrownBy(() -> holdManager.createHold(7L, 1L, List.of(10L, 20L), ttl))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hold Redis");

        verify(seat10).delete();
        verify(meta).delete();
    }

    @Test
    void release는_중복좌석을_정렬해_같은_holdKey만_삭제한다() {
        //given
        RBucket<Object> seat10 = bucketReturning("hold-key");
        RBucket<Object> seat20 = bucketReturning("other-hold");
        RBucket<Object> meta = mock(RBucket.class);

        when(redissonClient.getBucket(SeatRedisKey.hold(1L, 10L), StringCodec.INSTANCE)).thenReturn(seat10);
        when(redissonClient.getBucket(SeatRedisKey.hold(1L, 20L), StringCodec.INSTANCE)).thenReturn(seat20);
        when(redissonClient.getBucket(SeatRedisKey.holdMeta("hold-key"), StringCodec.INSTANCE)).thenReturn(meta);

        //when
        holdManager.release(1L, "hold-key", List.of(20L, 10L, 10L));

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
                .thenReturn(List.of(
                        SeatRedisKey.hold(1L, 30L),
                        SeatRedisKey.hold(1L, 10L)
                ));

        //when
        Set<Long> result = holdManager.getHoldingSeatIds(1L);

        //then
        assertThat(result).containsExactlyInAnyOrder(10L, 30L);
    }

    @Test
    void isHeld는_bucket값_존재여부를_반환한다() {
        //given
        RBucket<Object> seatBucket = bucketReturning("hold-key");
        when(redissonClient.getBucket(SeatRedisKey.hold(1L, 10L), StringCodec.INSTANCE)).thenReturn(seatBucket);

        //when
        boolean result = holdManager.isHeld(1L, 10L);

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

