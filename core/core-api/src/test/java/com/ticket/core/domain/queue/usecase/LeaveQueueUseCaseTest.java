package com.ticket.core.domain.queue.usecase;

import com.ticket.core.domain.queue.command.QueueAdvanceProcessor;
import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.domain.queue.runtime.QueueEntryRuntime;
import com.ticket.core.domain.queue.runtime.QueueRuntimeStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class LeaveQueueUseCaseTest {

    @Mock
    private QueueRuntimeStore queueRuntimeStore;

    @Mock
    private QueueAdvanceProcessor queueAdvanceProcessor;

    @InjectMocks
    private LeaveQueueUseCase leaveQueueUseCase;

    @Test
    void 대기중_엔트리는_대기열에서_제거한다() {
        when(queueRuntimeStore.findEntry("qe-wait")).thenReturn(Optional.of(
                new QueueEntryRuntime(10L, "qe-wait", QueueEntryStatus.WAITING, 1L, null, null)
        ));

        leaveQueueUseCase.execute(new LeaveQueueUseCase.Input(10L, "qe-wait"));

        verify(queueRuntimeStore).leaveWaiting(10L, "qe-wait");
    }

    @Test
    void 입장된_엔트리는_토큰을_회수하고_다음_대기자를_입장시킨다() {
        when(queueRuntimeStore.findEntry("qe-admit")).thenReturn(Optional.of(
                new QueueEntryRuntime(
                        10L,
                        "qe-admit",
                        QueueEntryStatus.ADMITTED,
                        null,
                        "qt-admit",
                        LocalDateTime.of(2026, 3, 15, 20, 40)
                )
        ));

        leaveQueueUseCase.execute(new LeaveQueueUseCase.Input(10L, "qe-admit"));

        verify(queueRuntimeStore).leaveAdmitted(10L, "qe-admit", "qt-admit");
        verify(queueAdvanceProcessor).advance(10L);
    }

    @Test
    void 엔트리가_없으면_아무_작업도_하지_않는다() {
        when(queueRuntimeStore.findEntry("missing")).thenReturn(Optional.empty());

        leaveQueueUseCase.execute(new LeaveQueueUseCase.Input(10L, "missing"));

        verify(queueRuntimeStore, never()).leaveWaiting(10L, "missing");
        verify(queueAdvanceProcessor, never()).advance(10L);
    }

    @Test
    void 다른_공연의_엔트리면_아무_작업도_하지_않는다() {
        when(queueRuntimeStore.findEntry("qe-other")).thenReturn(Optional.of(
                new QueueEntryRuntime(99L, "qe-other", QueueEntryStatus.WAITING, 1L, null, null)
        ));

        leaveQueueUseCase.execute(new LeaveQueueUseCase.Input(10L, "qe-other"));

        verify(queueRuntimeStore, never()).leaveWaiting(10L, "qe-other");
        verify(queueAdvanceProcessor, never()).advance(10L);
    }

    @Test
    void 입장상태여도_토큰이_없으면_아무_작업도_하지_않는다() {
        when(queueRuntimeStore.findEntry("qe-no-token")).thenReturn(Optional.of(
                new QueueEntryRuntime(10L, "qe-no-token", QueueEntryStatus.ADMITTED, null, null, null)
        ));

        leaveQueueUseCase.execute(new LeaveQueueUseCase.Input(10L, "qe-no-token"));

        verify(queueRuntimeStore, never()).leaveAdmitted(10L, "qe-no-token", null);
        verify(queueAdvanceProcessor, never()).advance(10L);
    }
}
