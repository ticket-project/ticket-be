package com.ticket.core.domain.queue.usecase;

import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.domain.queue.model.QueueLevel;
import com.ticket.core.domain.queue.runtime.QueueEntryLifecycleService;
import com.ticket.core.domain.queue.runtime.QueueEntryRuntime;
import com.ticket.core.domain.queue.runtime.QueueRuntimeStore;
import com.ticket.core.domain.queue.support.QueuePolicyResolver;
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
    private QueueEntryLifecycleService queueEntryLifecycleService;

    @InjectMocks
    private QueueEntryUseCase queueEntryUseCase;

    @Test
    void 대기열이_비활성화된_공연은_즉시_입장시킨다() {
        //given
        ResolvedQueuePolicy policy = new ResolvedQueuePolicy(
                false,
                QueueLevel.LEVEL_1,
                300,
                Duration.ofMinutes(10),
                Duration.ofHours(1)
        );
        QueueEntryRuntime admitted = createAdmitted("qe-3", "qt-3");

        when(queuePolicyResolver.resolve(10L)).thenReturn(policy);
        when(queueRuntimeStore.admitNow(10L, 101L, Duration.ofMinutes(10), Duration.ofHours(1))).thenReturn(admitted);

        //when
        QueueEntryUseCase.Output output = queueEntryUseCase.execute(new QueueEntryUseCase.Input(10L, 101L));

        //then
        assertThat(output.status()).isEqualTo(QueueEntryStatus.ADMITTED);
        assertThat(output.queueToken()).isEqualTo("qt-3");
        verify(queueRuntimeStore).admitNow(10L, 101L, Duration.ofMinutes(10), Duration.ofHours(1));
    }

    @Test
    void active가_최대인원보다_적으면_즉시_입장시킨다() {
        //given
        ResolvedQueuePolicy policy = createPolicy(300);
        QueueEntryRuntime admitted = createAdmitted("qe-1", "qt-1");

        when(queuePolicyResolver.resolve(10L)).thenReturn(policy);
        when(queueRuntimeStore.countActive(10L)).thenReturn(10L);
        when(queueRuntimeStore.admitNow(10L, 101L, Duration.ofMinutes(10), Duration.ofHours(1))).thenReturn(admitted);

        //when
        QueueEntryUseCase.Output output = queueEntryUseCase.execute(new QueueEntryUseCase.Input(10L, 101L));

        //then
        assertThat(output.status()).isEqualTo(QueueEntryStatus.ADMITTED);
        assertThat(output.queueEntryId()).isEqualTo("qe-1");
        assertThat(output.position()).isNull();
    }

    @Test
    void 최대인원을_초과하면_대기열에_등록한다() {
        //given
        ResolvedQueuePolicy policy = createPolicy(1);
        QueueEntryRuntime waiting = new QueueEntryRuntime(10L, 101L, "qe-2", QueueEntryStatus.WAITING, 2L, null, null);

        when(queuePolicyResolver.resolve(10L)).thenReturn(policy);
        when(queueRuntimeStore.countActive(10L)).thenReturn(1L);
        when(queueRuntimeStore.enqueue(10L, 101L, Duration.ofHours(1))).thenReturn(waiting);
        when(queueRuntimeStore.findWaitingPosition(10L, "qe-2")).thenReturn(Optional.of(2L));

        //when
        QueueEntryUseCase.Output output = queueEntryUseCase.execute(new QueueEntryUseCase.Input(10L, 101L));

        //then
        assertThat(output.status()).isEqualTo(QueueEntryStatus.WAITING);
        assertThat(output.position()).isEqualTo(2L);
        assertThat(output.estimatedWaitSeconds()).isEqualTo(1200L);
        assertThat(output.queueToken()).isNull();
    }

    @Test
    void 대기순번을_찾지_못하면_1번으로_처리한다() {
        //given
        ResolvedQueuePolicy policy = createPolicy(1);
        QueueEntryRuntime waiting = new QueueEntryRuntime(10L, 101L, "qe-2", QueueEntryStatus.WAITING, 2L, null, null);

        when(queuePolicyResolver.resolve(10L)).thenReturn(policy);
        when(queueRuntimeStore.countActive(10L)).thenReturn(1L);
        when(queueRuntimeStore.enqueue(10L, 101L, Duration.ofHours(1))).thenReturn(waiting);
        when(queueRuntimeStore.findWaitingPosition(10L, "qe-2")).thenReturn(Optional.empty());

        //when
        QueueEntryUseCase.Output output = queueEntryUseCase.execute(new QueueEntryUseCase.Input(10L, 101L));

        //then
        assertThat(output.position()).isEqualTo(1L);
        assertThat(output.estimatedWaitSeconds()).isEqualTo(600L);
    }

    @Test
    void 같은_회원이_재진입하면_기존_대기_엔트리를_정리하고_새로_대기열에_넣는다() {
        //given
        ResolvedQueuePolicy policy = createPolicy(1);
        QueueEntryRuntime previous = new QueueEntryRuntime(10L, 101L, "qe-old", QueueEntryStatus.WAITING, 1L, null, null);
        QueueEntryRuntime waiting = new QueueEntryRuntime(10L, 101L, "qe-new", QueueEntryStatus.WAITING, 2L, null, null);

        when(queuePolicyResolver.resolve(10L)).thenReturn(policy);
        when(queueRuntimeStore.countActive(10L)).thenReturn(1L);
        when(queueRuntimeStore.enqueue(10L, 101L, Duration.ofHours(1))).thenReturn(waiting);
        when(queueRuntimeStore.findWaitingPosition(10L, "qe-new")).thenReturn(Optional.of(2L));

        //when
        QueueEntryUseCase.Output output = queueEntryUseCase.execute(new QueueEntryUseCase.Input(10L, 101L));

        //then
        verify(queueEntryLifecycleService).cleanupForReentry(10L, 101L);
        assertThat(output.queueEntryId()).isEqualTo("qe-new");
    }

    @Test
    void 같은_회원이_입장상태에서_재진입하면_기존_토큰을_회수하고_승격후_새_엔트리를_발급한다() {
        //given
        ResolvedQueuePolicy policy = createPolicy(2);
        QueueEntryRuntime waiting = new QueueEntryRuntime(10L, 101L, "qe-new", QueueEntryStatus.WAITING, 3L, null, null);

        when(queuePolicyResolver.resolve(10L)).thenReturn(policy);
        when(queueRuntimeStore.countActive(10L)).thenReturn(2L);
        when(queueRuntimeStore.enqueue(10L, 101L, Duration.ofHours(1))).thenReturn(waiting);
        when(queueRuntimeStore.findWaitingPosition(10L, "qe-new")).thenReturn(Optional.of(4L));

        //when
        QueueEntryUseCase.Output output = queueEntryUseCase.execute(new QueueEntryUseCase.Input(10L, 101L));

        //then
        verify(queueEntryLifecycleService).cleanupForReentry(10L, 101L);
        assertThat(output.status()).isEqualTo(QueueEntryStatus.WAITING);
        assertThat(output.queueEntryId()).isEqualTo("qe-new");
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
                101L,
                queueEntryId,
                QueueEntryStatus.ADMITTED,
                null,
                queueToken,
                LocalDateTime.of(2026, 3, 15, 20, 10)
        );
    }
}

