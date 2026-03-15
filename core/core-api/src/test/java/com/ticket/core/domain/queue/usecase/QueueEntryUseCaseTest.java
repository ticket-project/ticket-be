package com.ticket.core.domain.queue.usecase;

import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.domain.queue.model.QueueLevel;
import com.ticket.core.domain.queue.runtime.QueueEntryRuntime;
import com.ticket.core.domain.queue.runtime.QueueRuntimeStore;
import com.ticket.core.domain.queue.support.QueuePolicyResolver;
import com.ticket.core.domain.queue.support.QueueWaitTimeEstimator;
import com.ticket.core.domain.queue.support.ResolvedQueuePolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class QueueEntryUseCaseTest {

    @Mock
    private QueuePolicyResolver queuePolicyResolver;

    @Mock
    private QueueRuntimeStore queueRuntimeStore;

    @Mock
    private QueueWaitTimeEstimator queueWaitTimeEstimator;

    @InjectMocks
    private QueueEntryUseCase queueEntryUseCase;

    @Test
    void 대기열이_비활성화된_공연은_즉시_입장시킨다() {
        ResolvedQueuePolicy policy = new ResolvedQueuePolicy(
                false,
                QueueLevel.LEVEL_1,
                300,
                Duration.ofMinutes(10),
                Duration.ofHours(1)
        );
        QueueEntryRuntime admitted = createAdmitted("qe-3", "qt-3");

        when(queuePolicyResolver.resolve(10L)).thenReturn(policy);
        when(queueRuntimeStore.admitNow(10L, Duration.ofMinutes(10), Duration.ofHours(1))).thenReturn(admitted);

        QueueEntryUseCase.Output output = queueEntryUseCase.execute(new QueueEntryUseCase.Input(10L));

        assertThat(output.status()).isEqualTo(QueueEntryStatus.ADMITTED);
        assertThat(output.queueToken()).isEqualTo("qt-3");
        verify(queueRuntimeStore).admitNow(10L, Duration.ofMinutes(10), Duration.ofHours(1));
    }

    @Test
    void active가_최대인원보다_적으면_즉시_입장시킨다() {
        ResolvedQueuePolicy policy = createPolicy(300);
        QueueEntryRuntime admitted = createAdmitted("qe-1", "qt-1");

        when(queuePolicyResolver.resolve(10L)).thenReturn(policy);
        when(queueRuntimeStore.countActive(10L)).thenReturn(10L);
        when(queueRuntimeStore.admitNow(10L, Duration.ofMinutes(10), Duration.ofHours(1))).thenReturn(admitted);

        QueueEntryUseCase.Output output = queueEntryUseCase.execute(new QueueEntryUseCase.Input(10L));

        assertThat(output.status()).isEqualTo(QueueEntryStatus.ADMITTED);
        assertThat(output.queueEntryId()).isEqualTo("qe-1");
        assertThat(output.position()).isNull();
    }

    @Test
    void 최대인원을_초과하면_대기열에_등록한다() {
        ResolvedQueuePolicy policy = createPolicy(1);
        QueueEntryRuntime waiting = new QueueEntryRuntime(10L, "qe-2", QueueEntryStatus.WAITING, 2L, null, null);

        when(queuePolicyResolver.resolve(10L)).thenReturn(policy);
        when(queueRuntimeStore.countActive(10L)).thenReturn(1L);
        when(queueRuntimeStore.enqueue(10L, Duration.ofHours(1))).thenReturn(waiting);
        when(queueRuntimeStore.findWaitingPosition(10L, "qe-2")).thenReturn(Optional.of(2L));
        when(queueWaitTimeEstimator.estimateSeconds(2L, 1, Duration.ofMinutes(10))).thenReturn(600L);

        QueueEntryUseCase.Output output = queueEntryUseCase.execute(new QueueEntryUseCase.Input(10L));

        assertThat(output.status()).isEqualTo(QueueEntryStatus.WAITING);
        assertThat(output.position()).isEqualTo(2L);
        assertThat(output.estimatedWaitSeconds()).isEqualTo(600L);
        assertThat(output.queueToken()).isNull();
    }

    @Test
    void 대기순번을_찾지_못하면_1번으로_처리한다() {
        ResolvedQueuePolicy policy = createPolicy(1);
        QueueEntryRuntime waiting = new QueueEntryRuntime(10L, "qe-2", QueueEntryStatus.WAITING, 2L, null, null);

        when(queuePolicyResolver.resolve(10L)).thenReturn(policy);
        when(queueRuntimeStore.countActive(10L)).thenReturn(1L);
        when(queueRuntimeStore.enqueue(10L, Duration.ofHours(1))).thenReturn(waiting);
        when(queueRuntimeStore.findWaitingPosition(10L, "qe-2")).thenReturn(Optional.empty());
        when(queueWaitTimeEstimator.estimateSeconds(1L, 1, Duration.ofMinutes(10))).thenReturn(600L);

        QueueEntryUseCase.Output output = queueEntryUseCase.execute(new QueueEntryUseCase.Input(10L));

        assertThat(output.position()).isEqualTo(1L);
        assertThat(output.estimatedWaitSeconds()).isEqualTo(600L);
    }

    private ResolvedQueuePolicy createPolicy(final int maxActiveUsers) {
        return new ResolvedQueuePolicy(
                true,
                QueueLevel.LEVEL_1,
                maxActiveUsers,
                Duration.ofMinutes(10),
                Duration.ofHours(1)
        );
    }

    private QueueEntryRuntime createAdmitted(final String queueEntryId, final String queueToken) {
        return new QueueEntryRuntime(
                10L,
                queueEntryId,
                QueueEntryStatus.ADMITTED,
                null,
                queueToken,
                LocalDateTime.of(2026, 3, 15, 20, 10)
        );
    }
}
