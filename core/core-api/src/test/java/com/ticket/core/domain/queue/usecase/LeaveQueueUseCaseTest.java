package com.ticket.core.domain.queue.usecase;

import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.domain.queue.runtime.QueueEntryLifecycleService;
import com.ticket.core.domain.queue.runtime.QueueEntryRuntime;
import com.ticket.core.domain.queue.runtime.QueueRuntimeStore;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class LeaveQueueUseCaseTest {

    @Mock
    private QueueRuntimeStore queueRuntimeStore;

    @Mock
    private QueueEntryLifecycleService queueEntryLifecycleService;

    @InjectMocks
    private LeaveQueueUseCase leaveQueueUseCase;

    @Test
    void 대기중_엔트리는_대기열에서_제거한다() {
        //given
        when(queueRuntimeStore.findEntry("qe-wait")).thenReturn(Optional.of(
                new QueueEntryRuntime(10L, 100L, "qe-wait", QueueEntryStatus.WAITING, 1L, null, null)
        ));

        //when
        leaveQueueUseCase.execute(new LeaveQueueUseCase.Input(10L, 100L, "qe-wait"));

        //then
        verify(queueEntryLifecycleService).leave(10L, 100L, new QueueEntryRuntime(10L, 100L, "qe-wait", QueueEntryStatus.WAITING, 1L, null, null));
    }

    @Test
    void 입장된_엔트리는_토큰을_회수하고_다음_대기자를_입장시킨다() {
        //given
        when(queueRuntimeStore.findEntry("qe-admit")).thenReturn(Optional.of(
                new QueueEntryRuntime(
                        10L,
                        100L,
                        "qe-admit",
                        QueueEntryStatus.ADMITTED,
                        null,
                        "qt-admit",
                        LocalDateTime.of(2026, 3, 15, 20, 40)
                )
        ));

        //when
        leaveQueueUseCase.execute(new LeaveQueueUseCase.Input(10L, 100L, "qe-admit"));

        //then
        verify(queueEntryLifecycleService).leave(
                10L,
                100L,
                new QueueEntryRuntime(10L, 100L, "qe-admit", QueueEntryStatus.ADMITTED, null, "qt-admit", LocalDateTime.of(2026, 3, 15, 20, 40))
        );
    }

    @Test
    void 엔트리가_없으면_아무_작업도_하지_않는다() {
        //given
        when(queueRuntimeStore.findEntry("missing")).thenReturn(Optional.empty());

        //when
        leaveQueueUseCase.execute(new LeaveQueueUseCase.Input(10L, 100L, "missing"));

        //then
        verify(queueEntryLifecycleService, never()).leave(10L, 100L, null);
    }

    @Test
    void 다른_공연의_엔트리면_아무_작업도_하지_않는다() {
        //given
        when(queueRuntimeStore.findEntry("qe-other")).thenReturn(Optional.of(
                new QueueEntryRuntime(99L, 100L, "qe-other", QueueEntryStatus.WAITING, 1L, null, null)
        ));

        //when
        leaveQueueUseCase.execute(new LeaveQueueUseCase.Input(10L, 100L, "qe-other"));

        //then
        verify(queueEntryLifecycleService, never()).leave(10L, 100L, new QueueEntryRuntime(99L, 100L, "qe-other", QueueEntryStatus.WAITING, 1L, null, null));
    }

    @Test
    void 입장상태는_유효한_토큰과_만료시간이_함께_전달된다() {
        assertThatThrownBy(() -> new QueueEntryRuntime(10L, 100L, "qe-no-token", QueueEntryStatus.ADMITTED, null, null, null))
                .isInstanceOf(CoreException.class)
                .satisfies(thrown -> assertThat(((CoreException) thrown).getErrorType()).isEqualTo(ErrorType.INVALID_REQUEST));
    }

}

