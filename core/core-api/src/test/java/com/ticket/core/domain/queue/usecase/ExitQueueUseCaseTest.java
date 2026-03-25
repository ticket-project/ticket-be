package com.ticket.core.domain.queue.usecase;

import com.ticket.core.domain.queue.command.QueueAdmissionAdvancer;
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
    private QueueAdmissionAdvancer queueAdmissionAdvancer;

    @InjectMocks
    private ExitQueueUseCase exitQueueUseCase;

    @Test
    void 대기중_엔트리는_대기열에서_제거한다() {
        QueueEntryId queueEntryId = QueueEntryId.from("qe-wait");
        when(queueTicketStore.findEntry(queueEntryId)).thenReturn(Optional.of(
                new QueueTicket(10L, 100L, "qe-wait", QueueEntryStatus.WAITING, 1L, null, null)
        ));

        exitQueueUseCase.execute(new ExitQueueUseCase.Input(10L, 100L, queueEntryId));

        verify(queueTicketStore).leaveWaiting(10L, queueEntryId);
        verify(queueAdmissionAdvancer, never()).advance(10L);
    }

    @Test
    void 입장_엔트리는_토큰을_회수하고_다음_대기자를_입장시킨다() {
        QueueEntryId queueEntryId = QueueEntryId.from("qe-admit");
        when(queueTicketStore.findEntry(queueEntryId)).thenReturn(Optional.of(
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

        exitQueueUseCase.execute(new ExitQueueUseCase.Input(10L, 100L, queueEntryId));

        verify(queueTicketStore).leaveAdmitted(10L, queueEntryId, "qt-admit");
        verify(queueAdmissionAdvancer).advance(10L);
    }

    @Test
    void 엔트리가_없으면_아무_작업도_하지_않는다() {
        QueueEntryId queueEntryId = QueueEntryId.from("missing");
        when(queueTicketStore.findEntry(queueEntryId)).thenReturn(Optional.empty());

        exitQueueUseCase.execute(new ExitQueueUseCase.Input(10L, 100L, queueEntryId));

        verify(queueAdmissionAdvancer, never()).advance(10L);
    }

    @Test
    void 다른_공연_엔트리면_아무_작업도_하지_않는다() {
        QueueEntryId queueEntryId = QueueEntryId.from("qe-other");
        when(queueTicketStore.findEntry(queueEntryId)).thenReturn(Optional.of(
                new QueueTicket(99L, 100L, "qe-other", QueueEntryStatus.WAITING, 1L, null, null)
        ));

        exitQueueUseCase.execute(new ExitQueueUseCase.Input(10L, 100L, queueEntryId));

        verify(queueTicketStore, never()).leaveWaiting(10L, queueEntryId);
        verify(queueAdmissionAdvancer, never()).advance(10L);
    }

    @Test
    void 입장_상태는_유효한_토큰과_만료시간을_강제한다() {
        assertThatThrownBy(() -> new QueueTicket(10L, 100L, "qe-no-token", QueueEntryStatus.ADMITTED, null, null, null))
                .isInstanceOf(CoreException.class)
                .satisfies(thrown -> assertThat(((CoreException) thrown).getErrorType()).isEqualTo(ErrorType.INVALID_REQUEST));
    }
}
