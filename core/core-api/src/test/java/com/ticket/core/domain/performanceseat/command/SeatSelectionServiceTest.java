package com.ticket.core.domain.performanceseat.command;

import com.ticket.core.domain.performanceseat.support.SeatRedisKey;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class SeatSelectionServiceTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RBucket<String> bucket;

    @Mock
    private org.redisson.api.RKeys rKeys;

    @InjectMocks
    private SeatSelectionService seatSelectionService;

    @Test
    void 빈_좌석이면_선택한다() {
        //given
        doReturn(bucket).when(redissonClient).getBucket(SeatRedisKey.select(10L, 20L), StringCodec.INSTANCE);
        when(bucket.setIfAbsent("3", Duration.ofMinutes(5))).thenReturn(true);

        //when
        seatSelectionService.select(10L, 20L, 3L);

        //then
        verify(bucket).setIfAbsent("3", Duration.ofMinutes(5));
    }

    @Test
    void 이미_선택된_좌석이면_예외를_던진다() {
        //given
        doReturn(bucket).when(redissonClient).getBucket(SeatRedisKey.select(10L, 20L), StringCodec.INSTANCE);
        when(bucket.setIfAbsent("3", Duration.ofMinutes(5))).thenReturn(false);

        //when
        //then
        assertThatThrownBy(() -> seatSelectionService.select(10L, 20L, 3L))
                .isInstanceOf(CoreException.class)
                .satisfies(thrown -> assertThat(((CoreException) thrown).getErrorType()).isEqualTo(ErrorType.SEAT_ALREADY_SELECTED));
    }

    @Test
    void 선택한_정보가_없으면_해제를_건너뛴다() {
        //given
        doReturn(bucket).when(redissonClient).getBucket(SeatRedisKey.select(10L, 20L), StringCodec.INSTANCE);
        when(bucket.get()).thenReturn(null);

        //when
        seatSelectionService.deselect(10L, 20L, 3L);

        //then
        verify(bucket, never()).compareAndSet("3", null);
    }

    @Test
    void 다른_회원이_선택한_좌석은_해제할_수_없다() {
        //given
        doReturn(bucket).when(redissonClient).getBucket(SeatRedisKey.select(10L, 20L), StringCodec.INSTANCE);
        when(bucket.get()).thenReturn("4");

        //when
        //then
        assertThatThrownBy(() -> seatSelectionService.deselect(10L, 20L, 3L))
                .isInstanceOf(CoreException.class)
                .satisfies(thrown -> assertThat(((CoreException) thrown).getErrorType()).isEqualTo(ErrorType.SEAT_NOT_OWNED));
    }

    @Test
    void 본인이_선택한_좌석은_해제한다() {
        //given
        doReturn(bucket).when(redissonClient).getBucket(SeatRedisKey.select(10L, 20L), StringCodec.INSTANCE);
        when(bucket.get()).thenReturn("3");
        when(bucket.compareAndSet("3", null)).thenReturn(true);

        //when
        seatSelectionService.deselect(10L, 20L, 3L);

        //then
        verify(bucket).compareAndSet("3", null);
    }

    @Test
    void 본인이_선택한_좌석만_일괄_해제한다() {
        //given
        //when
        when(redissonClient.getKeys()).thenReturn(rKeys);
        when(rKeys.getKeysByPattern(SeatRedisKey.selectPattern(10L))).thenReturn(List.of(
                SeatRedisKey.select(10L, 20L),
                SeatRedisKey.select(10L, 21L)
        ));
        doReturn(bucket).when(redissonClient).getBucket(SeatRedisKey.select(10L, 20L), StringCodec.INSTANCE);
        RBucket<String> otherBucket = org.mockito.Mockito.mock(RBucket.class);
        doReturn(otherBucket).when(redissonClient).getBucket(SeatRedisKey.select(10L, 21L), StringCodec.INSTANCE);
        when(bucket.get()).thenReturn("3");
        when(bucket.compareAndSet("3", null)).thenReturn(true);
        when(otherBucket.get()).thenReturn("4");

        //then
        assertThat(seatSelectionService.deselectAll(10L, 3L)).containsExactly(20L);
    }
}

