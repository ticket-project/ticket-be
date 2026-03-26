package com.ticket.core.domain.queue.command.join;

import com.ticket.core.domain.queue.command.QueueAdmissionAdvancer;
import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.domain.queue.runtime.QueueTicket;
import com.ticket.core.domain.queue.runtime.QueueTicketStore;
import com.ticket.core.domain.queue.model.QueueEntryId;
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
class QueueReentryCleanerTest {

    @Mock
    private QueueTicketStore queueTicketStore;

    @Mock
    private QueueAdmissionAdvancer queueAdmissionAdvancer;

    @InjectMocks
    private QueueReentryCleaner queueReentryCleaner;

    @Test
    void 재진입_대기회원을_정리한다() {
        final QueueTicket previous = new QueueTicket(10L, 101L, "qe-old", QueueEntryStatus.WAITING, 1L, null, null);
        when(queueTicketStore.findMemberEntryId(10L, 101L)).thenReturn(Optional.of("qe-old"));
        when(queueTicketStore.findEntry(QueueEntryId.from("qe-old"))).thenReturn(Optional.of(previous));

        queueReentryCleaner.cleanup(10L, 101L);

        verify(queueTicketStore).leaveWaiting(10L, QueueEntryId.from("qe-old"));
        verify(queueAdmissionAdvancer, never()).advance(10L);
    }

    @Test
    void 재진입_입장회원을_정리하고_다음대기자를_입장시킨다() {
        final QueueTicket previous = new QueueTicket(
                10L,
                101L,
                "qe-old",
                QueueEntryStatus.ADMITTED,
                null,
                "qt-old",
                LocalDateTime.of(2026, 3, 15, 20, 0)
        );
        when(queueTicketStore.findMemberEntryId(10L, 101L)).thenReturn(Optional.of("qe-old"));
        when(queueTicketStore.findEntry(QueueEntryId.from("qe-old"))).thenReturn(Optional.of(previous));

        queueReentryCleaner.cleanup(10L, 101L);

        verify(queueTicketStore).leaveAdmitted(10L, QueueEntryId.from("qe-old"), "qt-old");
        verify(queueAdmissionAdvancer).advance(10L);
    }

    @Test
    void 이전_엔트리가_없으면_멤버매핑을_정리한다() {
        when(queueTicketStore.findMemberEntryId(10L, 101L)).thenReturn(Optional.of("missing"));
        when(queueTicketStore.findEntry(QueueEntryId.from("missing"))).thenReturn(Optional.empty());

        queueReentryCleaner.cleanup(10L, 101L);

        verify(queueTicketStore).clearMemberEntry(10L, 101L);
    }

    @Test
    void 이전_엔트리가_이미종료상태면_멤버매핑을_정리한다() {
        final QueueTicket previous = new QueueTicket(10L, 101L, "qe-old", QueueEntryStatus.LEFT, 1L, null, null);
        when(queueTicketStore.findMemberEntryId(10L, 101L)).thenReturn(Optional.of("qe-old"));
        when(queueTicketStore.findEntry(QueueEntryId.from("qe-old"))).thenReturn(Optional.of(previous));

        queueReentryCleaner.cleanup(10L, 101L);

        verify(queueTicketStore).clearMemberEntry(10L, 101L);
        verify(queueAdmissionAdvancer, never()).advance(10L);
    }
}
