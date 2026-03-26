package com.ticket.core.domain.queue.query.status;

import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.domain.queue.runtime.QueueTicket;
import com.ticket.core.domain.queue.runtime.QueueTicketStore;
import com.ticket.core.domain.queue.model.QueueEntryId;
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
class QueueStatusReaderTest {

    @Mock
    private QueueTicketStore queueTicketStore;

    @InjectMocks
    private QueueStatusReader queueStatusReader;

    @Test
    void 엔트리가_없으면_EXPIRED를_반환한다() {
        final QueueEntryId queueEntryId = QueueEntryId.from("qe-10");
        final GetQueueStatusUseCase.Input input = new GetQueueStatusUseCase.Input(10L, 100L, queueEntryId);
        when(queueTicketStore.findEntry(queueEntryId)).thenReturn(Optional.empty());

        final GetQueueStatusUseCase.Output output = queueStatusReader.read(input);

        assertThat(output.status()).isEqualTo(QueueEntryStatus.EXPIRED);
        assertThat(output.queueToken()).isNull();
    }

    @Test
    void 대기중이면_현재_순번을_반환한다() {
        final QueueEntryId queueEntryId = QueueEntryId.from("qe-10");
        final GetQueueStatusUseCase.Input input = new GetQueueStatusUseCase.Input(10L, 100L, queueEntryId);
        final QueueTicket waiting = new QueueTicket(10L, 100L, "qe-10", QueueEntryStatus.WAITING, 5L, null, null);
        when(queueTicketStore.findEntry(queueEntryId)).thenReturn(Optional.of(waiting));
        when(queueTicketStore.findWaitingPosition(10L, queueEntryId)).thenReturn(Optional.of(5L));

        final GetQueueStatusUseCase.Output output = queueStatusReader.read(input);

        assertThat(output.status()).isEqualTo(QueueEntryStatus.WAITING);
        assertThat(output.position()).isEqualTo(5L);
    }

    @Test
    void 대기순번을_찾지_못하면_0을_기본값으로_쓴다() {
        final QueueEntryId queueEntryId = QueueEntryId.from("qe-10");
        final GetQueueStatusUseCase.Input input = new GetQueueStatusUseCase.Input(10L, 100L, queueEntryId);
        final QueueTicket waiting = new QueueTicket(10L, 100L, "qe-10", QueueEntryStatus.WAITING, 5L, null, null);
        when(queueTicketStore.findEntry(queueEntryId)).thenReturn(Optional.of(waiting));
        when(queueTicketStore.findWaitingPosition(10L, queueEntryId)).thenReturn(Optional.empty());

        final GetQueueStatusUseCase.Output output = queueStatusReader.read(input);

        assertThat(output.position()).isEqualTo(0L);
    }

    @Test
    void 입장토큰이_만료되면_EXPIRED를_반환한다() {
        final QueueEntryId queueEntryId = QueueEntryId.from("qe-11");
        final GetQueueStatusUseCase.Input input = new GetQueueStatusUseCase.Input(10L, 100L, queueEntryId);
        final QueueTicket admitted = new QueueTicket(
                10L,
                100L,
                "qe-11",
                QueueEntryStatus.ADMITTED,
                null,
                "qt-11",
                LocalDateTime.of(2026, 3, 15, 20, 20)
        );
        when(queueTicketStore.findEntry(queueEntryId)).thenReturn(Optional.of(admitted));
        when(queueTicketStore.isValidToken(10L, "qt-11")).thenReturn(false);

        final GetQueueStatusUseCase.Output output = queueStatusReader.read(input);

        assertThat(output.status()).isEqualTo(QueueEntryStatus.EXPIRED);
        assertThat(output.queueToken()).isNull();
    }

    @Test
    void 입장토큰이_유효하면_입장정보를_반환한다() {
        final QueueEntryId queueEntryId = QueueEntryId.from("qe-11");
        final GetQueueStatusUseCase.Input input = new GetQueueStatusUseCase.Input(10L, 100L, queueEntryId);
        final QueueTicket admitted = new QueueTicket(
                10L,
                100L,
                "qe-11",
                QueueEntryStatus.ADMITTED,
                null,
                "qt-11",
                LocalDateTime.of(2026, 3, 15, 20, 20)
        );
        when(queueTicketStore.findEntry(queueEntryId)).thenReturn(Optional.of(admitted));
        when(queueTicketStore.isValidToken(10L, "qt-11")).thenReturn(true);

        final GetQueueStatusUseCase.Output output = queueStatusReader.read(input);

        assertThat(output.status()).isEqualTo(QueueEntryStatus.ADMITTED);
        assertThat(output.queueToken()).isEqualTo("qt-11");
        assertThat(output.expiresAt()).isEqualTo(LocalDateTime.of(2026, 3, 15, 20, 20));
    }

    @Test
    void LEFT_상태는_그대로_반환한다() {
        final QueueEntryId queueEntryId = QueueEntryId.from("qe-12");
        final GetQueueStatusUseCase.Input input = new GetQueueStatusUseCase.Input(10L, 100L, queueEntryId);
        final QueueTicket left = new QueueTicket(10L, 100L, "qe-12", QueueEntryStatus.LEFT, 1L, null, null);
        when(queueTicketStore.findEntry(queueEntryId)).thenReturn(Optional.of(left));

        final GetQueueStatusUseCase.Output output = queueStatusReader.read(input);

        assertThat(output.status()).isEqualTo(QueueEntryStatus.LEFT);
    }

    @Test
    void 다른회원의_엔트리를_조회하면_예외가_발생한다() {
        final QueueEntryId queueEntryId = QueueEntryId.from("qe-10");
        final GetQueueStatusUseCase.Input input = new GetQueueStatusUseCase.Input(10L, 100L, queueEntryId);
        final QueueTicket waiting = new QueueTicket(10L, 999L, "qe-10", QueueEntryStatus.WAITING, 5L, null, null);
        when(queueTicketStore.findEntry(queueEntryId)).thenReturn(Optional.of(waiting));

        assertThatThrownBy(() -> queueStatusReader.read(input))
                .isInstanceOf(AuthException.class);
    }
}
