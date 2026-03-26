package com.ticket.core.domain.queue.command.exit;

import com.ticket.core.domain.queue.command.QueueAdmissionAdvancer;
import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.domain.queue.runtime.QueueTicket;
import com.ticket.core.domain.queue.runtime.QueueTicketStore;
import com.ticket.core.domain.queue.model.QueueEntryId;
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
class QueueExitProcessorTest {

    @Mock
    private QueueTicketStore queueTicketStore;

    @Mock
    private QueueAdmissionAdvancer queueAdmissionAdvancer;

    @InjectMocks
    private QueueExitProcessor queueExitProcessor;

    @Test
    void 대기_엔트리는_대기열에서_제거한다() {
        final QueueEntryId queueEntryId = QueueEntryId.from("qe-wait");
        final ExitQueueUseCase.Input input = new ExitQueueUseCase.Input(10L, 100L, queueEntryId);
        when(queueTicketStore.findEntry(queueEntryId)).thenReturn(Optional.of(
                new QueueTicket(10L, 100L, "qe-wait", QueueEntryStatus.WAITING, 1L, null, null)
        ));

        queueExitProcessor.exit(input);

        verify(queueTicketStore).leaveWaiting(10L, queueEntryId);
        verify(queueAdmissionAdvancer, never()).advance(10L);
    }

    @Test
    void 입장_엔트리는_토큰을_회수하고_다음_대기자를_입장시킨다() {
        final QueueEntryId queueEntryId = QueueEntryId.from("qe-admit");
        final ExitQueueUseCase.Input input = new ExitQueueUseCase.Input(10L, 100L, queueEntryId);
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

        queueExitProcessor.exit(input);

        verify(queueTicketStore).leaveAdmitted(10L, queueEntryId, "qt-admit");
        verify(queueAdmissionAdvancer).advance(10L);
    }

    @Test
    void 엔트리가_없으면_아무작업도_하지않는다() {
        final QueueEntryId queueEntryId = QueueEntryId.from("missing");
        final ExitQueueUseCase.Input input = new ExitQueueUseCase.Input(10L, 100L, queueEntryId);
        when(queueTicketStore.findEntry(queueEntryId)).thenReturn(Optional.empty());

        queueExitProcessor.exit(input);

        verify(queueAdmissionAdvancer, never()).advance(10L);
    }

    @Test
    void 다른_공연_엔트리면_아무작업도_하지않는다() {
        final QueueEntryId queueEntryId = QueueEntryId.from("qe-other");
        final ExitQueueUseCase.Input input = new ExitQueueUseCase.Input(10L, 100L, queueEntryId);
        when(queueTicketStore.findEntry(queueEntryId)).thenReturn(Optional.of(
                new QueueTicket(99L, 100L, "qe-other", QueueEntryStatus.WAITING, 1L, null, null)
        ));

        queueExitProcessor.exit(input);

        verify(queueTicketStore, never()).leaveWaiting(10L, queueEntryId);
        verify(queueAdmissionAdvancer, never()).advance(10L);
    }

    @Test
    void 입장_상태는_유효한_토큰과_만료시간이_필수다() {
        assertThatThrownBy(() -> new QueueTicket(10L, 100L, "qe-no-token", QueueEntryStatus.ADMITTED, null, null, null))
                .isInstanceOf(CoreException.class)
                .satisfies(thrown -> assertThat(((CoreException) thrown).getErrorType()).isEqualTo(ErrorType.INVALID_REQUEST));
    }
}
