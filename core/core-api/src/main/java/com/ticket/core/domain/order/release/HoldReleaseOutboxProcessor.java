package com.ticket.core.domain.order.release;

import com.ticket.core.domain.hold.support.HoldManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class HoldReleaseOutboxProcessor {

    private static final Duration RETRY_DELAY = Duration.ofSeconds(30);

    private final HoldReleaseOutboxRepository holdReleaseOutboxRepository;
    private final HoldManager holdManager;
    private final Clock clock;

    @Transactional
    public void process(final Long outboxId) {
        final HoldReleaseOutbox outbox = holdReleaseOutboxRepository.findById(outboxId)
                .orElse(null);
        if (outbox == null || outbox.isCompleted()) {
            return;
        }

        try {
            holdManager.release(outbox.getPerformanceId(), outbox.getHoldKey(), outbox.seatIds());
            outbox.markCompleted(LocalDateTime.now(clock));
        } catch (final RuntimeException e) {
            outbox.scheduleRetry(LocalDateTime.now(clock).plus(RETRY_DELAY), e.getMessage());
            log.error("hold release outbox 처리 실패: outboxId={}, holdKey={}", outboxId, outbox.getHoldKey(), e);
        }
    }
}
