package com.ticket.core.domain.queue.support;

import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceFinder;
import com.ticket.core.domain.queue.model.QueueLevel;
import com.ticket.core.domain.queue.model.QueueMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class QueuePolicyResolverTest {

    @Mock
    private PerformanceFinder performanceFinder;

    @InjectMocks
    private QueuePolicyResolver queuePolicyResolver;

    @BeforeEach
    void setUp() {
        QueueProperties properties = new QueueProperties();
        properties.setEnabledByDefault(true);
        properties.setDefaultLevel(QueueLevel.LEVEL_1);
        properties.setDefaultMaxActiveUsers(300);
        properties.setDefaultEntryTokenTtl(Duration.ofMinutes(10));
        properties.setEntryRetention(Duration.ofHours(1));
        queuePolicyResolver = new QueuePolicyResolver(performanceFinder, properties);
    }

    @Test
    void 정책이_없으면_기본값으로_해결한다() {
        //given
        when(performanceFinder.findById(10L)).thenReturn(createPerformance());

        //when
        QueuePolicy resolved = queuePolicyResolver.resolve(10L);

        //then
        assertThat(resolved.enabled()).isTrue();
        assertThat(resolved.queueLevel()).isEqualTo(QueueLevel.LEVEL_1);
        assertThat(resolved.maxActiveUsers()).isEqualTo(300);
        assertThat(resolved.entryTokenTtl()).isEqualTo(Duration.ofMinutes(10));
        assertThat(resolved.entryRetention()).isEqualTo(Duration.ofHours(1));
        verify(performanceFinder).findById(10L);
    }

    @Test
    void FORCE_OFF면_기본설정보다_우선해_비활성화한다() {
        //given
        Performance performance = createPerformance();
        performance.updateQueuePolicy(
                QueueMode.FORCE_OFF,
                QueueLevel.LEVEL_2,
                500,
                900,
                LocalDateTime.of(2026, 3, 15, 19, 50),
                "대기열 운영",
                "사유"
        );
        when(performanceFinder.findById(10L)).thenReturn(performance);

        //when
        QueuePolicy resolved = queuePolicyResolver.resolve(10L);

        //then
        assertThat(resolved.enabled()).isFalse();
        assertThat(resolved.queueLevel()).isEqualTo(QueueLevel.LEVEL_2);
        assertThat(resolved.maxActiveUsers()).isEqualTo(500);
        assertThat(resolved.entryTokenTtl()).isEqualTo(Duration.ofSeconds(900));
    }

    @Test
    void FORCE_ON이면_기본설정이_false여도_활성화한다() {
        //given
        QueueProperties properties = new QueueProperties();
        properties.setEnabledByDefault(false);
        properties.setDefaultLevel(QueueLevel.LEVEL_1);
        properties.setDefaultMaxActiveUsers(300);
        properties.setDefaultEntryTokenTtl(Duration.ofMinutes(10));
        properties.setEntryRetention(Duration.ofHours(1));
        queuePolicyResolver = new QueuePolicyResolver(performanceFinder, properties);
        Performance performance = createPerformance();
        performance.updateQueuePolicy(
                QueueMode.FORCE_ON,
                null,
                null,
                null,
                null,
                null,
                null
        );
        when(performanceFinder.findById(10L)).thenReturn(performance);

        //when
        QueuePolicy resolved = queuePolicyResolver.resolve(10L);

        //then
        assertThat(resolved.enabled()).isTrue();
        assertThat(resolved.queueLevel()).isEqualTo(QueueLevel.LEVEL_1);
        assertThat(resolved.maxActiveUsers()).isEqualTo(300);
        assertThat(resolved.entryTokenTtl()).isEqualTo(Duration.ofMinutes(10));
    }

    private Performance createPerformance() {
        return new Performance(
                null,
                1L,
                LocalDateTime.of(2026, 3, 16, 10, 0),
                LocalDateTime.of(2026, 3, 16, 12, 0),
                LocalDateTime.of(2026, 3, 15, 10, 0),
                LocalDateTime.of(2026, 3, 16, 9, 0),
                4,
                420
        );
    }
}
