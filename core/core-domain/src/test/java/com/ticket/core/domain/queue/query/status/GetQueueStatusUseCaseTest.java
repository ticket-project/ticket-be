package com.ticket.core.domain.queue.query.status;

import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.domain.queue.model.QueueEntryId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class GetQueueStatusUseCaseTest {

    @Mock
    private QueueStatusReader queueStatusReader;

    private GetQueueStatusUseCase getQueueStatusUseCase;

    @BeforeEach
    void setUp() {
        getQueueStatusUseCase = new GetQueueStatusUseCase(queueStatusReader);
    }

    @Test
    void 상태조회_처리를_reader에_위임한다() {
        final QueueEntryId queueEntryId = QueueEntryId.from("qe-10");
        final GetQueueStatusUseCase.Input input = new GetQueueStatusUseCase.Input(10L, 100L, queueEntryId);
        final GetQueueStatusUseCase.Output output = new GetQueueStatusUseCase.Output(
                QueueEntryStatus.EXPIRED,
                "qe-10",
                null,
                null,
                null
        );
        when(queueStatusReader.read(input)).thenReturn(output);

        final GetQueueStatusUseCase.Output result = getQueueStatusUseCase.execute(input);

        assertThat(result).isSameAs(output);
        verify(queueStatusReader).read(input);
    }
}
