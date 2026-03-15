package com.ticket.core.domain.queue.model;

import com.ticket.core.domain.performance.Performance;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SuppressWarnings("NonAsciiCharacters")
class PerformanceQueuePolicyTest {

    @Test
    void 정책을_생성하면_입력값을_보관한다() {
        //given
        LocalDateTime preopen = LocalDateTime.of(2026, 3, 15, 19, 50);

        //when
        PerformanceQueuePolicy policy = new PerformanceQueuePolicy(
                mock(Performance.class),
                QueueMode.AUTO,
                QueueLevel.LEVEL_1,
                300,
                600,
                preopen,
                "대기열 운영",
                "초기 정책"
        );

        //then
        assertThat(policy.getQueueMode()).isEqualTo(QueueMode.AUTO);
        assertThat(policy.getQueueLevel()).isEqualTo(QueueLevel.LEVEL_1);
        assertThat(policy.getMaxActiveUsers()).isEqualTo(300);
        assertThat(policy.getEntryTokenTtlSeconds()).isEqualTo(600);
        assertThat(policy.getPreopenQueueStartAt()).isEqualTo(preopen);
        assertThat(policy.getWaitingRoomMessage()).isEqualTo("대기열 운영");
        assertThat(policy.getReason()).isEqualTo("초기 정책");
    }

    @Test
    void update는_정책값을_덮어쓴다() {
        //given
        PerformanceQueuePolicy policy = new PerformanceQueuePolicy(
                mock(Performance.class),
                QueueMode.AUTO,
                QueueLevel.LEVEL_1,
                300,
                600,
                null,
                null,
                null
        );
        LocalDateTime preopen = LocalDateTime.of(2026, 3, 15, 19, 50);

        //when
        policy.update(
                QueueMode.FORCE_ON,
                QueueLevel.LEVEL_2,
                500,
                900,
                preopen,
                "대기열 운영",
                "수동 변경"
        );

        //then
        assertThat(policy.getQueueMode()).isEqualTo(QueueMode.FORCE_ON);
        assertThat(policy.getQueueLevel()).isEqualTo(QueueLevel.LEVEL_2);
        assertThat(policy.getMaxActiveUsers()).isEqualTo(500);
        assertThat(policy.getEntryTokenTtlSeconds()).isEqualTo(900);
        assertThat(policy.getPreopenQueueStartAt()).isEqualTo(preopen);
        assertThat(policy.getWaitingRoomMessage()).isEqualTo("대기열 운영");
        assertThat(policy.getReason()).isEqualTo("수동 변경");
    }
}

