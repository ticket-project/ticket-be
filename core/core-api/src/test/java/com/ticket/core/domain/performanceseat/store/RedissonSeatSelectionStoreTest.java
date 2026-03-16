package com.ticket.core.domain.performanceseat.store;

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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class RedissonSeatSelectionStoreTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RBucket<String> bucket;

    @Mock
    private RKeys rKeys;

    @InjectMocks
    private RedissonSeatSelectionStore redissonSeatSelectionStore;

    @Test
    void 비어있는_좌석이면_selectIfAbsent가_true다() {
        //given
        doReturn(bucket).when(redissonClient).getBucket(SeatRedisKey.select(10L, 20L), StringCodec.INSTANCE);
        when(bucket.setIfAbsent("3", Duration.ofMinutes(5))).thenReturn(true);

        //when
        boolean result = redissonSeatSelectionStore.selectIfAbsent(10L, 20L, "3", Duration.ofMinutes(5));

        //then
        assertThat(result).isTrue();
        verify(bucket).setIfAbsent("3", Duration.ofMinutes(5));
    }

    @Test
    void holder를_조회하고_소유자가_맞으면_해제한다() {
        //given
        doReturn(bucket).when(redissonClient).getBucket(SeatRedisKey.select(10L, 20L), StringCodec.INSTANCE);
        when(bucket.get()).thenReturn("3");
        when(bucket.compareAndSet("3", null)).thenReturn(true);

        //when
        String holder = redissonSeatSelectionStore.getHolder(10L, 20L);
        boolean released = redissonSeatSelectionStore.releaseIfOwned(10L, 20L, "3");

        //then
        assertThat(holder).isEqualTo("3");
        assertThat(released).isTrue();
    }

    @Test
    void 소유한_좌석만_일괄_해제한다() {
        //given
        when(redissonClient.getKeys()).thenReturn(rKeys);
        when(rKeys.getKeysByPattern(SeatRedisKey.selectPattern(10L))).thenReturn(List.of(
                SeatRedisKey.select(10L, 20L),
                SeatRedisKey.select(10L, 21L)
        ));
        doReturn(bucket).when(redissonClient).getBucket(SeatRedisKey.select(10L, 20L), StringCodec.INSTANCE);
        RBucket<String> otherBucket = mock(RBucket.class);
        doReturn(otherBucket).when(redissonClient).getBucket(SeatRedisKey.select(10L, 21L), StringCodec.INSTANCE);
        when(bucket.get()).thenReturn("3");
        when(bucket.compareAndSet("3", null)).thenReturn(true);
        when(otherBucket.get()).thenReturn("4");

        //when
        List<Long> result = redissonSeatSelectionStore.releaseAllByMember(10L, "3");

        //then
        assertThat(result).containsExactly(20L);
    }
}
