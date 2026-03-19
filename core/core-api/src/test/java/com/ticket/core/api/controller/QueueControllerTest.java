package com.ticket.core.api.controller;

import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.enums.Role;
import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.domain.queue.usecase.GetQueueStatusUseCase;
import com.ticket.core.domain.queue.usecase.ExitQueueUseCase;
import com.ticket.core.domain.queue.usecase.JoinQueueUseCase;
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

    private static final MemberPrincipal MEMBER = new MemberPrincipal(100L, Role.MEMBER);

    @Mock
    private JoinQueueUseCase joinQueueUseCase;

    @Mock
    private GetQueueStatusUseCase getQueueStatusUseCase;

    @Mock
    private ExitQueueUseCase exitQueueUseCase;

    @InjectMocks
    private QueueController queueController;

    @Test
    void 대기열_진입시_usecase_output을_그대로_반환한다() {
        // given
        final JoinQueueUseCase.Output output = new JoinQueueUseCase.Output(
                QueueEntryStatus.WAITING,
                "qe-1",
                3L,
                null,
                null
        );
        when(joinQueueUseCase.execute(new JoinQueueUseCase.Input(10L, 100L))).thenReturn(output);

        // when
        final ApiResponse<JoinQueueUseCase.Output> response = queueController.enter(10L, MEMBER);

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
                "token-1",
                snapshot.expiresAt
        );
        when(getQueueStatusUseCase.execute(new GetQueueStatusUseCase.Input(20L, 100L, "qe-2"))).thenReturn(output);

        // when
        final ApiResponse<GetQueueStatusUseCase.Output> response = queueController.getStatus(20L, "qe-2", MEMBER);

        // then
        assertThat(response.getData()).isSameAs(output);
    }

    @Test
    void 대기열_이탈시_usecase를_호출하고_성공응답을_반환한다() {
        // when
        final ApiResponse<Void> response = queueController.leave(30L, "qe-3", MEMBER);

        // then
        verify(exitQueueUseCase).execute(new ExitQueueUseCase.Input(30L, 100L, "qe-3"));
        assertThat(response.getData()).isNull();
    }

    private static final class QueueStatusSnapshot {
        private final LocalDateTime expiresAt = LocalDateTime.of(2026, 3, 16, 9, 0);
    }
}
