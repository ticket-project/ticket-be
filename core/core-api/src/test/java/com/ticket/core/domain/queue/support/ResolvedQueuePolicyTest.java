package com.ticket.core.domain.queue.support;

import com.ticket.core.domain.queue.model.QueueLevel;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
class ResolvedQueuePolicyTest {

    @Test
    void 활성사용자가_최대보다_적으면_즉시입장할_수_있다() {
        //given
        ResolvedQueuePolicy policy = new ResolvedQueuePolicy(
                true,
                QueueLevel.LEVEL_1,
                2,
                Duration.ofMinutes(10),
                Duration.ofHours(1)
        );

        //when //then
        assertThat(policy.shouldAdmitImmediately(1L)).isTrue();
        assertThat(policy.shouldAdmitImmediately(2L)).isFalse();
    }

    @Test
    void 비활성화_정책이면_항상_즉시입장이다() {
        //given
        ResolvedQueuePolicy policy = new ResolvedQueuePolicy(
                false,
                QueueLevel.LEVEL_1,
                2,
                Duration.ofMinutes(10),
                Duration.ofHours(1)
        );

        //when //then
        assertThat(policy.shouldAdmitImmediately(999L)).isTrue();
    }

    @Test
    void 예상대기시간을_직접_계산한다() {
        //given
        ResolvedQueuePolicy policy = new ResolvedQueuePolicy(
                true,
                QueueLevel.LEVEL_1,
                2,
                Duration.ofMinutes(10),
                Duration.ofHours(1)
        );

        //when //then
        assertThat(policy.estimateWaitSeconds(5L)).isEqualTo(1800L);
    }
}
