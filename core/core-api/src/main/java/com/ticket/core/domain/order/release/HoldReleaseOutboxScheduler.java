package com.ticket.core.domain.order.release;

import com.ticket.core.aop.DistributedLock;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class HoldReleaseOutboxScheduler {

    private static final int BATCH_SIZE = 100;

    private final HoldReleaseOutboxRepository holdReleaseOutboxRepository;
    private final HoldReleaseOutboxProcessor holdReleaseOutboxProcessor;
    private final Clock clock;

    @Scheduled(fixedDelayString = "120000")
    @DistributedLock(
            prefix = "hold-release-outbox",
            dynamicKey = "'batch'",
            leaseTime = 60000L,
            message = "hold release outbox 처리 중입니다. 잠시 후 다시 시도해 주세요."
    )
    public void processPendingHoldReleases() {
        while (true) {
            final Slice<HoldReleaseOutbox> dueOutboxes = holdReleaseOutboxRepository.findAllByCompletedAtIsNullAndNextAttemptAtLessThanEqual(
                    LocalDateTime.now(clock),
                    PageRequest.of(0, BATCH_SIZE, Sort.by(Sort.Direction.ASC, "id"))
            );
            if (!dueOutboxes.hasContent()) {
                return;
            }

            for (final HoldReleaseOutbox outbox : dueOutboxes.getContent()) {
                holdReleaseOutboxProcessor.process(outbox.getId(), LocalDateTime.now(clock));
            }

            if (dueOutboxes.getNumberOfElements() < BATCH_SIZE) {
                return;
            }
        }
    }
}
