package com.ticket.core.domain.queue.command.exit;

import com.ticket.core.domain.queue.model.QueueEntryId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class ExitQueueUseCaseTest {

    @Mock
    private QueueExitProcessor queueExitProcessor;

    private ExitQueueUseCase exitQueueUseCase;

    @BeforeEach
    void setUp() {
        exitQueueUseCase = new ExitQueueUseCase(queueExitProcessor);
    }

    @Test
    void 퇴장처리를_processor에_위임한다() {
        final ExitQueueUseCase.Input input = new ExitQueueUseCase.Input(10L, 100L, QueueEntryId.from("qe-wait"));

        exitQueueUseCase.execute(input);

        verify(queueExitProcessor).exit(input);
    }
}
