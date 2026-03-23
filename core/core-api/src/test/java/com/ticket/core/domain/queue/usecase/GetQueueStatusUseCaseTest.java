package com.ticket.core.domain.queue.usecase;

import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.domain.queue.runtime.QueueTicket;
import com.ticket.core.domain.queue.runtime.QueueTicketStore;
import com.ticket.core.support.exception.AuthException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class GetQueueStatusUseCaseTest {

    @Mock
    private QueueTicketStore queueTicketStore;

    @InjectMocks
    private GetQueueStatusUseCase getQueueStatusUseCase;

    @Test
    void 엔트리가_없으면_EXPIRED를_반환한다() {
        //given
        QueueEntryId queueEntryId = QueueEntryId.from("qe-10");
        when(queueTicketStore.findEntry(queueEntryId)).thenReturn(Optional.empty());

        //when
        GetQueueStatusUseCase.Output output = getQueueStatusUseCase.execute(new GetQueueStatusUseCase.Input(10L, 100L, queueEntryId));

        //then
        assertThat(output.status()).isEqualTo(QueueEntryStatus.EXPIRED);
        assertThat(output.queueToken()).isNull();
    }

    @Test
    void 대기중_엔트리는_현재_순번과_예상대기시간을_반환한다() {
        //given
        QueueEntryId queueEntryId = QueueEntryId.from("qe-10");
        QueueTicket waiting = new QueueTicket(10L, 100L, "qe-10", QueueEntryStatus.WAITING, 5L, null, null);
        when(queueTicketStore.findEntry(queueEntryId)).thenReturn(Optional.of(waiting));
        when(queueTicketStore.findWaitingPosition(10L, queueEntryId)).thenReturn(Optional.of(5L));

        //when
        GetQueueStatusUseCase.Output output = getQueueStatusUseCase.execute(new GetQueueStatusUseCase.Input(10L, 100L, queueEntryId));

        //then
        assertThat(output.status()).isEqualTo(QueueEntryStatus.WAITING);
        assertThat(output.position()).isEqualTo(5L);
    }

    @Test
    void 대기순번을_찾지_못하면_0초기값으로_계산한다() {
        //given
        QueueEntryId queueEntryId = QueueEntryId.from("qe-10");
        QueueTicket waiting = new QueueTicket(10L, 100L, "qe-10", QueueEntryStatus.WAITING, 5L, null, null);
        when(queueTicketStore.findEntry(queueEntryId)).thenReturn(Optional.of(waiting));
        when(queueTicketStore.findWaitingPosition(10L, queueEntryId)).thenReturn(Optional.empty());

        //when
        GetQueueStatusUseCase.Output output = getQueueStatusUseCase.execute(new GetQueueStatusUseCase.Input(10L, 100L, queueEntryId));

        //then
        assertThat(output.position()).isEqualTo(0L);
    }

    @Test
    void 입장된_엔트리의_토큰이_만료되면_EXPIRED를_반환한다() {
        //given
        QueueTicket admitted = new QueueTicket(
                10L,
                100L,
                "qe-11",
                QueueEntryStatus.ADMITTED,
                null,
                "qt-11",
                LocalDateTime.of(2026, 3, 15, 20, 20)
        );
        QueueEntryId queueEntryId = QueueEntryId.from("qe-11");
        when(queueTicketStore.findEntry(queueEntryId)).thenReturn(Optional.of(admitted));
        when(queueTicketStore.isValidToken(10L, "qt-11")).thenReturn(false);

        //when
        GetQueueStatusUseCase.Output output = getQueueStatusUseCase.execute(new GetQueueStatusUseCase.Input(10L, 100L, queueEntryId));

        //then
        assertThat(output.status()).isEqualTo(QueueEntryStatus.EXPIRED);
        assertThat(output.queueToken()).isNull();
    }

    @Test
    void 입장된_엔트리의_토큰이_유효하면_입장정보를_반환한다() {
        //given
        QueueTicket admitted = new QueueTicket(
                10L,
                100L,
                "qe-11",
                QueueEntryStatus.ADMITTED,
                null,
                "qt-11",
                LocalDateTime.of(2026, 3, 15, 20, 20)
        );
        QueueEntryId queueEntryId = QueueEntryId.from("qe-11");
        when(queueTicketStore.findEntry(queueEntryId)).thenReturn(Optional.of(admitted));
        when(queueTicketStore.isValidToken(10L, "qt-11")).thenReturn(true);

        //when
        GetQueueStatusUseCase.Output output = getQueueStatusUseCase.execute(new GetQueueStatusUseCase.Input(10L, 100L, queueEntryId));

        //then
        assertThat(output.status()).isEqualTo(QueueEntryStatus.ADMITTED);
        assertThat(output.queueToken()).isEqualTo("qt-11");
        assertThat(output.expiresAt()).isEqualTo(LocalDateTime.of(2026, 3, 15, 20, 20));
    }

    @Test
    void LEFT_상태_엔트리는_그대로_반환한다() {
        //given
        QueueEntryId queueEntryId = QueueEntryId.from("qe-12");
        QueueTicket left = new QueueTicket(10L, 100L, "qe-12", QueueEntryStatus.LEFT, 1L, null, null);
        when(queueTicketStore.findEntry(queueEntryId)).thenReturn(Optional.of(left));

        //when
        GetQueueStatusUseCase.Output output = getQueueStatusUseCase.execute(new GetQueueStatusUseCase.Input(10L, 100L, queueEntryId));

        //then
        assertThat(output.status()).isEqualTo(QueueEntryStatus.LEFT);
    }

    @Test
    void 다른_회원의_엔트리를_조회하면_예외가_발생한다() {
        //given
        QueueEntryId queueEntryId = QueueEntryId.from("qe-10");
        QueueTicket waiting = new QueueTicket(10L, 999L, "qe-10", QueueEntryStatus.WAITING, 5L, null, null);
        when(queueTicketStore.findEntry(queueEntryId)).thenReturn(Optional.of(waiting));

        //when //then
        assertThatThrownBy(() -> getQueueStatusUseCase.execute(new GetQueueStatusUseCase.Input(10L, 100L, queueEntryId)))
                .isInstanceOf(AuthException.class);
    }

}

