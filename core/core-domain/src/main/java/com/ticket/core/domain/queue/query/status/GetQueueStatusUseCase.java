package com.ticket.core.domain.queue.query.status;

import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.domain.queue.model.QueueEntryId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GetQueueStatusUseCase {

    private final QueueStatusReader queueStatusReader;

    public record Input(Long performanceId, Long memberId, QueueEntryId queueEntryId) {}

    public record Output(
            QueueEntryStatus status,
            String queueEntryId,
            Long position,
            String queueToken,
            LocalDateTime expiresAt
    ) {}

    public Output execute(final Input input) {
        return queueStatusReader.read(input);
    }
}
