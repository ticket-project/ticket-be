package com.ticket.core.domain.queue.support;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
class QueueWaitTimeEstimatorTest {

    private final QueueWaitTimeEstimator queueWaitTimeEstimator = new QueueWaitTimeEstimator();

    @Test
    void 순번이_0이하면_대기시간은_0초다() {
        //given
        //when
        //then
        assertThat(queueWaitTimeEstimator.estimateSeconds(0L, 300, Duration.ofMinutes(10))).isZero();
        assertThat(queueWaitTimeEstimator.estimateSeconds(-1L, 300, Duration.ofMinutes(10))).isZero();
    }

    @Test
    void 최대활성인원이_0이하여도_최소_1로_계산한다() {
        //given
        //when
        //then
        assertThat(queueWaitTimeEstimator.estimateSeconds(2L, 0, Duration.ofMinutes(10))).isEqualTo(1200L);
    }

    @Test
    void 대기시간은_올림배치수에_토큰ttl을_곱해_계산한다() {
        //given
        //when
        //then
        assertThat(queueWaitTimeEstimator.estimateSeconds(5L, 2, Duration.ofMinutes(10))).isEqualTo(1800L);
    }
}

