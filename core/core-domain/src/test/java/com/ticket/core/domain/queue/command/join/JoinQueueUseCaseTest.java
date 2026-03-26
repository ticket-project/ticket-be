package com.ticket.core.domain.queue.command.join;

import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.domain.queue.model.QueueLevel;
import com.ticket.core.domain.queue.support.QueuePolicy;
import com.ticket.core.domain.queue.support.QueuePolicyResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class JoinQueueUseCaseTest {

    @Mock
    private QueuePolicyResolver queuePolicyResolver;

    @Mock
    private QueueReentryCleaner queueReentryCleaner;

    @Mock
    private QueueJoinProcessor queueJoinProcessor;

    private JoinQueueUseCase joinQueueUseCase;

    @BeforeEach
    void setUp() {
        joinQueueUseCase = new JoinQueueUseCase(queuePolicyResolver, queueReentryCleaner, queueJoinProcessor);
    }

    @Test
    void 정책조회와_재진입정리_후_입장처리를_위임한다() {
        final QueuePolicy policy = new QueuePolicy(false, QueueLevel.LEVEL_1, 300, Duration.ofMinutes(10), Duration.ofHours(1));
        final JoinQueueUseCase.Input input = new JoinQueueUseCase.Input(10L, 101L);
        final JoinQueueUseCase.Output output = new JoinQueueUseCase.Output(
                QueueEntryStatus.ADMITTED,
                "qe-3",
                null,
                "qt-3",
                LocalDateTime.of(2026, 3, 15, 19, 10)
        );
        when(queuePolicyResolver.resolve(10L)).thenReturn(policy);
        when(queueJoinProcessor.join(input, policy)).thenReturn(output);

        final JoinQueueUseCase.Output result = joinQueueUseCase.execute(input);

        assertThat(result).isSameAs(output);
        final InOrder inOrder = inOrder(queuePolicyResolver, queueReentryCleaner, queueJoinProcessor);
        inOrder.verify(queuePolicyResolver).resolve(10L);
        inOrder.verify(queueReentryCleaner).cleanup(10L, 101L);
        inOrder.verify(queueJoinProcessor).join(input, policy);
    }
}
