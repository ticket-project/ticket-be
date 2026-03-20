package com.ticket.core.domain.queue.usecase;

import com.ticket.core.domain.queue.command.QueueAdmissionProcessor;
import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.domain.queue.runtime.QueueTicket;
import com.ticket.core.domain.queue.runtime.QueueTicketStore;
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
class ExitQueueUseCaseTest {

    @Mock
    private QueueTicketStore queueTicketStore;

    @Mock
    private QueueAdmissionProcessor queueAdmissionProcessor;

    @InjectMocks
    private ExitQueueUseCase exitQueueUseCase;

    @Test
    void 대기중_엔트리는_대기열에서_제거한다() {
        when(queueTicketStore.findEntry("qe-wait")).thenReturn(Optional.of(
                new QueueTicket(10L, 100L, "qe-wait", QueueEntryStatus.WAITING, 1L, null, null)
        ));

        exitQueueUseCase.execute(new ExitQueueUseCase.Input(10L, 100L, QueueEntryId.from("qe-wait")));

        verify(queueTicketStore).leaveWaiting(10L, "qe-wait");
        verify(queueAdmissionProcessor, never()).advance(10L);
    }

    @Test
    void 입장_엔트리는_토큰을_회수하고_다음_대기자를_입장시킨다() {
        when(queueTicketStore.findEntry("qe-admit")).thenReturn(Optional.of(
                new QueueTicket(
                        10L,
                        100L,
                        "qe-admit",
                        QueueEntryStatus.ADMITTED,
                        null,
                        "qt-admit",
                        LocalDateTime.of(2026, 3, 15, 20, 40)
                )
        ));

        exitQueueUseCase.execute(new ExitQueueUseCase.Input(10L, 100L, QueueEntryId.from("qe-admit")));

        verify(queueTicketStore).leaveAdmitted(10L, "qe-admit", "qt-admit");
        verify(queueAdmissionProcessor).advance(10L);
    }

    @Test
    void 엔트리가_없으면_아무_작업도_하지_않는다() {
        when(queueTicketStore.findEntry("missing")).thenReturn(Optional.empty());

        exitQueueUseCase.execute(new ExitQueueUseCase.Input(10L, 100L, QueueEntryId.from("missing")));

        verify(queueAdmissionProcessor, never()).advance(10L);
    }

    @Test
    void 다른_공연_엔트리면_아무_작업도_하지_않는다() {
        when(queueTicketStore.findEntry("qe-other")).thenReturn(Optional.of(
                new QueueTicket(99L, 100L, "qe-other", QueueEntryStatus.WAITING, 1L, null, null)
        ));

        exitQueueUseCase.execute(new ExitQueueUseCase.Input(10L, 100L, QueueEntryId.from("qe-other")));

        verify(queueTicketStore, never()).leaveWaiting(10L, "qe-other");
        verify(queueAdmissionProcessor, never()).advance(10L);
    }

    @Test
    void 입장_상태는_유효한_토큰과_만료시간을_강제한다() {
        assertThatThrownBy(() -> new QueueTicket(10L, 100L, "qe-no-token", QueueEntryStatus.ADMITTED, null, null, null))
                .isInstanceOf(CoreException.class)
                .satisfies(thrown -> assertThat(((CoreException) thrown).getErrorType()).isEqualTo(ErrorType.INVALID_REQUEST));
    }
}
