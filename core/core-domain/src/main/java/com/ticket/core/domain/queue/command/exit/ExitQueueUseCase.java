package com.ticket.core.domain.queue.command.exit;

import com.ticket.core.aop.DistributedLock;
import com.ticket.core.domain.queue.model.QueueEntryId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExitQueueUseCase {

    private final QueueExitProcessor queueExitProcessor;

    public record Input(Long performanceId, Long memberId, QueueEntryId queueEntryId) {}

    @DistributedLock(
            prefix = "queue",
            dynamicKey = "#input.performanceId()",
            leaseTime = 5000L,
            message = "대기열 이탈 처리 중입니다. 잠시 후 다시 시도해 주세요."
    )
    public void execute(final Input input) {
        queueExitProcessor.exit(input);
    }
}
