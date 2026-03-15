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
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class GetQueueStatusUseCaseTest {

    @Mock
    private QueuePolicyResolver queuePolicyResolver;

    @Mock
    private QueueRuntimeStore queueRuntimeStore;

    @Mock
    private QueueWaitTimeEstimator queueWaitTimeEstimator;

    @InjectMocks
    private GetQueueStatusUseCase getQueueStatusUseCase;

    @Test
    void 엔트리가_없으면_EXPIRED를_반환한다() {
        //given
        when(queuePolicyResolver.resolve(10L)).thenReturn(createPolicy());
        when(queueRuntimeStore.findEntry("qe-10")).thenReturn(Optional.empty());

        //when
        GetQueueStatusUseCase.Output output = getQueueStatusUseCase.execute(new GetQueueStatusUseCase.Input(10L, "qe-10"));

        //then
        assertThat(output.status()).isEqualTo(QueueEntryStatus.EXPIRED);
        assertThat(output.queueToken()).isNull();
    }

    @Test
    void 대기중_엔트리는_현재_순번과_예상대기시간을_반환한다() {
        //given
        QueueEntryRuntime waiting = new QueueEntryRuntime(10L, "qe-10", QueueEntryStatus.WAITING, 5L, null, null);
        when(queuePolicyResolver.resolve(10L)).thenReturn(createPolicy());
        when(queueRuntimeStore.findEntry("qe-10")).thenReturn(Optional.of(waiting));
        when(queueRuntimeStore.findWaitingPosition(10L, "qe-10")).thenReturn(Optional.of(5L));
        when(queueWaitTimeEstimator.estimateSeconds(5L, 300, Duration.ofMinutes(10))).thenReturn(10L);

        //when
        GetQueueStatusUseCase.Output output = getQueueStatusUseCase.execute(new GetQueueStatusUseCase.Input(10L, "qe-10"));

        //then
        assertThat(output.status()).isEqualTo(QueueEntryStatus.WAITING);
        assertThat(output.position()).isEqualTo(5L);
        assertThat(output.estimatedWaitSeconds()).isEqualTo(10L);
    }

    @Test
    void 대기순번을_찾지_못하면_0초기값으로_계산한다() {
        //given
        QueueEntryRuntime waiting = new QueueEntryRuntime(10L, "qe-10", QueueEntryStatus.WAITING, 5L, null, null);
        when(queuePolicyResolver.resolve(10L)).thenReturn(createPolicy());
        when(queueRuntimeStore.findEntry("qe-10")).thenReturn(Optional.of(waiting));
        when(queueRuntimeStore.findWaitingPosition(10L, "qe-10")).thenReturn(Optional.empty());
        when(queueWaitTimeEstimator.estimateSeconds(0L, 300, Duration.ofMinutes(10))).thenReturn(0L);

        //when
        GetQueueStatusUseCase.Output output = getQueueStatusUseCase.execute(new GetQueueStatusUseCase.Input(10L, "qe-10"));

        //then
        assertThat(output.position()).isEqualTo(0L);
        assertThat(output.estimatedWaitSeconds()).isEqualTo(0L);
    }

    @Test
    void 입장된_엔트리의_토큰이_만료되면_EXPIRED를_반환한다() {
        //given
        QueueEntryRuntime admitted = new QueueEntryRuntime(
                10L,
                "qe-11",
                QueueEntryStatus.ADMITTED,
                null,
                "qt-11",
                LocalDateTime.of(2026, 3, 15, 20, 20)
        );
        when(queuePolicyResolver.resolve(10L)).thenReturn(createPolicy());
        when(queueRuntimeStore.findEntry("qe-11")).thenReturn(Optional.of(admitted));
        when(queueRuntimeStore.isValidToken(10L, "qt-11")).thenReturn(false);

        //when
        GetQueueStatusUseCase.Output output = getQueueStatusUseCase.execute(new GetQueueStatusUseCase.Input(10L, "qe-11"));

        //then
        assertThat(output.status()).isEqualTo(QueueEntryStatus.EXPIRED);
        assertThat(output.queueToken()).isNull();
    }

    @Test
    void 입장된_엔트리의_토큰이_유효하면_입장정보를_반환한다() {
        //given
        QueueEntryRuntime admitted = new QueueEntryRuntime(
                10L,
                "qe-11",
                QueueEntryStatus.ADMITTED,
                null,
                "qt-11",
                LocalDateTime.of(2026, 3, 15, 20, 20)
        );
        when(queuePolicyResolver.resolve(10L)).thenReturn(createPolicy());
        when(queueRuntimeStore.findEntry("qe-11")).thenReturn(Optional.of(admitted));
        when(queueRuntimeStore.isValidToken(10L, "qt-11")).thenReturn(true);

        //when
        GetQueueStatusUseCase.Output output = getQueueStatusUseCase.execute(new GetQueueStatusUseCase.Input(10L, "qe-11"));

        //then
        assertThat(output.status()).isEqualTo(QueueEntryStatus.ADMITTED);
        assertThat(output.queueToken()).isEqualTo("qt-11");
        assertThat(output.expiresAt()).isEqualTo(LocalDateTime.of(2026, 3, 15, 20, 20));
    }

    @Test
    void LEFT_상태_엔트리는_그대로_반환한다() {
        //given
        QueueEntryRuntime left = new QueueEntryRuntime(10L, "qe-12", QueueEntryStatus.LEFT, 1L, null, null);
        when(queuePolicyResolver.resolve(10L)).thenReturn(createPolicy());
        when(queueRuntimeStore.findEntry("qe-12")).thenReturn(Optional.of(left));

        //when
        GetQueueStatusUseCase.Output output = getQueueStatusUseCase.execute(new GetQueueStatusUseCase.Input(10L, "qe-12"));

        //then
        assertThat(output.status()).isEqualTo(QueueEntryStatus.LEFT);
    }

    private ResolvedQueuePolicy createPolicy() {
        return new ResolvedQueuePolicy(
                true,
                QueueLevel.LEVEL_1,
                300,
                Duration.ofMinutes(10),
                Duration.ofHours(1)
        );
    }
}

