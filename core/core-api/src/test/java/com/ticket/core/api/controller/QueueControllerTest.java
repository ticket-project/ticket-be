package com.ticket.core.api.controller;

import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.domain.queue.usecase.GetQueueStatusUseCase;
import com.ticket.core.domain.queue.usecase.LeaveQueueUseCase;
import com.ticket.core.domain.queue.usecase.QueueEntryUseCase;
import com.ticket.core.support.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class QueueControllerTest {

    @Mock
    private QueueEntryUseCase queueEntryUseCase;

    @Mock
    private GetQueueStatusUseCase getQueueStatusUseCase;

    @Mock
    private LeaveQueueUseCase leaveQueueUseCase;

    @InjectMocks
    private QueueController queueController;

    @Test
    void 대기열_진입시_usecase_output을_그대로_반환한다() {
        // given
        final QueueEntryUseCase.Output output = new QueueEntryUseCase.Output(
                QueueEntryStatus.WAITING,
                "qe-1",
                3L,
                120L,
                null,
                null
        );
        when(queueEntryUseCase.execute(new QueueEntryUseCase.Input(10L))).thenReturn(output);

        // when
        final ApiResponse<QueueEntryUseCase.Output> response = queueController.enter(10L);

        // then
        assertThat(response.getData()).isSameAs(output);
    }

    @Test
    void 대기열_상태조회시_usecase_output을_그대로_반환한다() {
        // given
        final QueueStatusSnapshot snapshot = new QueueStatusSnapshot();
        final GetQueueStatusUseCase.Output output = new GetQueueStatusUseCase.Output(
                QueueEntryStatus.ADMITTED,
                "qe-2",
                null,
                null,
                "token-1",
                snapshot.expiresAt
        );
        when(getQueueStatusUseCase.execute(new GetQueueStatusUseCase.Input(20L, "qe-2"))).thenReturn(output);

        // when
        final ApiResponse<GetQueueStatusUseCase.Output> response = queueController.getStatus(20L, "qe-2");

        // then
        assertThat(response.getData()).isSameAs(output);
    }

    @Test
    void 대기열_이탈시_usecase를_호출하고_성공응답을_반환한다() {
        // when
        final ApiResponse<Void> response = queueController.leave(30L, "qe-3");

        // then
        verify(leaveQueueUseCase).execute(new LeaveQueueUseCase.Input(30L, "qe-3"));
        assertThat(response.getData()).isNull();
    }

    private static final class QueueStatusSnapshot {
        private final LocalDateTime expiresAt = LocalDateTime.of(2026, 3, 16, 9, 0);
    }
}
