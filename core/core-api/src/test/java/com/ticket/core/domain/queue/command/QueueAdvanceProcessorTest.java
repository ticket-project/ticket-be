package com.ticket.core.domain.queue.command;

import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.domain.queue.model.QueueLevel;
import com.ticket.core.domain.queue.runtime.QueueEntryRuntime;
import com.ticket.core.domain.queue.runtime.QueueRuntimeStore;
import com.ticket.core.domain.queue.support.QueuePolicyResolver;
import com.ticket.core.domain.queue.support.ResolvedQueuePolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class QueueAdvanceProcessorTest {

    @Mock
    private QueuePolicyResolver queuePolicyResolver;

    @Mock
    private QueueRuntimeStore queueRuntimeStore;

    @InjectMocks
    private QueueAdvanceProcessor queueAdvanceProcessor;

    @Test
    void 빈자리가_있고_대기자가_있으면_다음_대기자를_입장시킨다() {
        //given
        ResolvedQueuePolicy policy = createPolicy(1);
        QueueEntryRuntime admitted = createAdmitted(10L, "qe-100", "qt-100");

        when(queuePolicyResolver.resolve(10L)).thenReturn(policy);
        when(queueRuntimeStore.countActive(10L)).thenReturn(0L, 1L);
        when(queueRuntimeStore.admitNextWaiting(10L, Duration.ofMinutes(10), Duration.ofHours(1)))
                .thenReturn(Optional.of(admitted));

        //when
        queueAdvanceProcessor.advance(10L);

        //then
        verify(queueRuntimeStore).admitNextWaiting(10L, Duration.ofMinutes(10), Duration.ofHours(1));
    }

    @Test
    void 이미_active가_가득차있으면_대기자를_입장시키지_않는다() {
        //given
        when(queuePolicyResolver.resolve(10L)).thenReturn(createPolicy(1));
        when(queueRuntimeStore.countActive(10L)).thenReturn(1L);

        //when
        queueAdvanceProcessor.advance(10L);

        //then
        verify(queueRuntimeStore, never()).admitNextWaiting(10L, Duration.ofMinutes(10), Duration.ofHours(1));
    }

    @Test
    void 대기자가_없으면_즉시_종료한다() {
        //given
        when(queuePolicyResolver.resolve(10L)).thenReturn(createPolicy(1));
        when(queueRuntimeStore.countActive(10L)).thenReturn(0L);
        when(queueRuntimeStore.admitNextWaiting(10L, Duration.ofMinutes(10), Duration.ofHours(1)))
                .thenReturn(Optional.empty());

        //when
        queueAdvanceProcessor.advance(10L);

        //then
        verify(queueRuntimeStore).admitNextWaiting(10L, Duration.ofMinutes(10), Duration.ofHours(1));
    }

    @Test
    void 토큰_만료를_처리한_후_다음_대기자를_입장시킨다() {
        //given
        when(queuePolicyResolver.resolve(10L)).thenReturn(createPolicy(1));
        when(queueRuntimeStore.countActive(10L)).thenReturn(0L, 1L);
        when(queueRuntimeStore.admitNextWaiting(10L, Duration.ofMinutes(10), Duration.ofHours(1)))
                .thenReturn(Optional.of(createAdmitted(10L, "qe-201", "qt-201")));

        //when
        queueAdvanceProcessor.handleTokenExpired(10L, "qe-200", "qt-200");

        //then
        verify(queueRuntimeStore).expireAdmitted(10L, "qe-200", "qt-200");
        verify(queueRuntimeStore).admitNextWaiting(10L, Duration.ofMinutes(10), Duration.ofHours(1));
    }

    private ResolvedQueuePolicy createPolicy(final int maxActiveUsers) {
        return new ResolvedQueuePolicy(
                true,
                QueueLevel.LEVEL_1,
                maxActiveUsers,
                Duration.ofMinutes(10),
                Duration.ofHours(1)
        );
    }

    private QueueEntryRuntime createAdmitted(final Long performanceId, final String entryId, final String token) {
        return new QueueEntryRuntime(
                performanceId,
                100L,
                entryId,
                QueueEntryStatus.ADMITTED,
                null,
                token,
                LocalDateTime.of(2026, 3, 15, 20, 30)
        );
    }
}

