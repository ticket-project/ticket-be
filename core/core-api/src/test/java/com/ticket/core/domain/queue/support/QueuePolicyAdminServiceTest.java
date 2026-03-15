package com.ticket.core.domain.queue.support;

import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceFinder;
import com.ticket.core.domain.queue.model.PerformanceQueuePolicy;
import com.ticket.core.domain.queue.model.QueueLevel;
import com.ticket.core.domain.queue.model.QueueMode;
import com.ticket.core.domain.queue.repository.PerformanceQueuePolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class QueuePolicyAdminServiceTest {

    @Mock
    private PerformanceQueuePolicyRepository performanceQueuePolicyRepository;

    @Mock
    private PerformanceFinder performanceFinder;

    @InjectMocks
    private QueuePolicyAdminService queuePolicyAdminService;

    @BeforeEach
    void setUp() {
        QueueProperties queueProperties = new QueueProperties();
        queueProperties.setEnabledByDefault(true);
        queueProperties.setDefaultLevel(QueueLevel.LEVEL_1);
        queueProperties.setDefaultMaxActiveUsers(300);
        queueProperties.setDefaultEntryTokenTtl(Duration.ofMinutes(10));
        queueProperties.setEntryRetention(Duration.ofHours(1));
        queuePolicyAdminService = new QueuePolicyAdminService(performanceQueuePolicyRepository, performanceFinder, queueProperties);
    }

    @Test
    void 설정이_없으면_기본값을_반환한다() {
        when(performanceQueuePolicyRepository.findByPerformanceId(10L)).thenReturn(Optional.empty());

        QueuePolicyAdminService.PolicyDetail detail = queuePolicyAdminService.get(10L);

        assertThat(detail.queueMode()).isEqualTo(QueueMode.AUTO);
        assertThat(detail.queueLevel()).isEqualTo(QueueLevel.LEVEL_1);
        assertThat(detail.maxActiveUsers()).isEqualTo(300);
        assertThat(detail.entryTokenTtlSeconds()).isEqualTo(600);
    }

    @Test
    void 기존_설정이_있으면_저장된_정책을_반환한다() {
        PerformanceQueuePolicy policy = createPolicy(
                QueueMode.FORCE_ON,
                QueueLevel.LEVEL_2,
                500,
                900,
                LocalDateTime.of(2026, 3, 15, 19, 50),
                "대기열 운영",
                "수동 조정"
        );
        when(performanceQueuePolicyRepository.findByPerformanceId(10L)).thenReturn(Optional.of(policy));

        QueuePolicyAdminService.PolicyDetail detail = queuePolicyAdminService.get(10L);

        assertThat(detail.queueMode()).isEqualTo(QueueMode.FORCE_ON);
        assertThat(detail.queueLevel()).isEqualTo(QueueLevel.LEVEL_2);
        assertThat(detail.maxActiveUsers()).isEqualTo(500);
        assertThat(detail.entryTokenTtlSeconds()).isEqualTo(900);
        assertThat(detail.waitingRoomMessage()).isEqualTo("대기열 운영");
    }

    @Test
    void 설정이_없으면_새_정책을_생성한다() {
        Performance performance = createPerformance();
        when(performanceFinder.findById(10L)).thenReturn(performance);
        when(performanceQueuePolicyRepository.findByPerformanceId(10L)).thenReturn(Optional.empty());
        when(performanceQueuePolicyRepository.save(any(PerformanceQueuePolicy.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        QueuePolicyAdminService.PolicyDetail detail = queuePolicyAdminService.upsert(10L, createUpdateCommand());

        ArgumentCaptor<PerformanceQueuePolicy> captor = ArgumentCaptor.forClass(PerformanceQueuePolicy.class);
        verify(performanceQueuePolicyRepository).save(captor.capture());
        assertThat(captor.getValue().getPerformance()).isSameAs(performance);
        assertThat(detail.queueLevel()).isEqualTo(QueueLevel.LEVEL_2);
        assertThat(detail.maxActiveUsers()).isEqualTo(500);
        assertThat(detail.entryTokenTtlSeconds()).isEqualTo(900);
    }

    @Test
    void 기존_정책이_있으면_업데이트한다() {
        Performance performance = createPerformance();
        PerformanceQueuePolicy existing = createPolicy(
                QueueMode.AUTO,
                QueueLevel.LEVEL_1,
                300,
                600,
                null,
                null,
                null
        );
        when(performanceFinder.findById(10L)).thenReturn(performance);
        when(performanceQueuePolicyRepository.findByPerformanceId(10L)).thenReturn(Optional.of(existing));
        when(performanceQueuePolicyRepository.save(existing)).thenReturn(existing);

        QueuePolicyAdminService.PolicyDetail detail = queuePolicyAdminService.upsert(10L, createUpdateCommand());

        assertThat(existing.getQueueMode()).isEqualTo(QueueMode.FORCE_ON);
        assertThat(existing.getQueueLevel()).isEqualTo(QueueLevel.LEVEL_2);
        assertThat(existing.getMaxActiveUsers()).isEqualTo(500);
        assertThat(detail.waitingRoomMessage()).isEqualTo("대기열 운영");
        assertThat(detail.reason()).isEqualTo("초기 운영");
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

    private PerformanceQueuePolicy createPolicy(
            final QueueMode mode,
            final QueueLevel level,
            final Integer maxActiveUsers,
            final Integer ttlSeconds,
            final LocalDateTime preopenQueueStartAt,
            final String waitingRoomMessage,
            final String reason
    ) {
        return new PerformanceQueuePolicy(
                createPerformance(),
                mode,
                level,
                maxActiveUsers,
                ttlSeconds,
                preopenQueueStartAt,
                waitingRoomMessage,
                reason
        );
    }

    private QueuePolicyAdminService.UpdateCommand createUpdateCommand() {
        return new QueuePolicyAdminService.UpdateCommand(
                QueueMode.FORCE_ON,
                QueueLevel.LEVEL_2,
                500,
                900,
                LocalDateTime.of(2026, 3, 15, 19, 50),
                "대기열 운영",
                "초기 운영"
        );
    }
}
