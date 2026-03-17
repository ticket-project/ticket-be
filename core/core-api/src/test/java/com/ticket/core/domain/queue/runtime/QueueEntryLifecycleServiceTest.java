package com.ticket.core.domain.queue.runtime;

import com.ticket.core.domain.queue.command.QueueAdvanceProcessor;
import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.support.exception.AuthException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class QueueEntryLifecycleServiceTest {

    @Mock
    private QueueRuntimeStore queueRuntimeStore;

    @Mock
    private QueueAdvanceProcessor queueAdvanceProcessor;

    @InjectMocks
    private QueueEntryLifecycleService queueEntryLifecycleService;

    @Test
    void 재진입시_기존_대기엔트리를_정리한다() {
        //given
        QueueEntryRuntime entry = new QueueEntryRuntime(10L, 100L, "qe-1", QueueEntryStatus.WAITING, 1L, null, null);
        when(queueRuntimeStore.findMemberEntryId(10L, 100L)).thenReturn(Optional.of("qe-1"));
        when(queueRuntimeStore.findEntry("qe-1")).thenReturn(Optional.of(entry));

        //when
        queueEntryLifecycleService.cleanupForReentry(10L, 100L);

        //then
        verify(queueRuntimeStore).leaveWaiting(10L, "qe-1");
        verify(queueAdvanceProcessor, never()).advance(10L);
    }

    @Test
    void 재진입시_기존_입장엔트리를_정리하고_승격한다() {
        //given
        QueueEntryRuntime entry = new QueueEntryRuntime(
                10L,
                100L,
                "qe-1",
                QueueEntryStatus.ADMITTED,
                1L,
                "qt-1",
                LocalDateTime.of(2026, 3, 17, 10, 0)
        );
        when(queueRuntimeStore.findMemberEntryId(10L, 100L)).thenReturn(Optional.of("qe-1"));
        when(queueRuntimeStore.findEntry("qe-1")).thenReturn(Optional.of(entry));

        //when
        queueEntryLifecycleService.cleanupForReentry(10L, 100L);

        //then
        verify(queueRuntimeStore).leaveAdmitted(10L, "qe-1", "qt-1");
        verify(queueAdvanceProcessor).advance(10L);
    }

    @Test
    void 다른_회원_엔트리를_이탈할_수_없다() {
        //given
        QueueEntryRuntime entry = new QueueEntryRuntime(10L, 999L, "qe-1", QueueEntryStatus.WAITING, 1L, null, null);

        //when //then
        assertThatThrownBy(() -> queueEntryLifecycleService.leave(10L, 100L, entry))
                .isInstanceOf(AuthException.class);
    }
}
