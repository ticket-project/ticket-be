package com.ticket.core.domain.order.release;

import com.ticket.core.domain.hold.support.HoldManager;
import com.ticket.core.domain.performanceseat.application.SeatStatusPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class HoldReleaseOutboxExecutor {

    private static final Duration RETRY_DELAY = Duration.ofSeconds(30);

    private final HoldReleaseOutboxRepository holdReleaseOutboxRepository;
    private final HoldManager holdManager;
    private final SeatStatusPublisher seatStatusPublisher;

    @Transactional
    public void process(final Long outboxId, final LocalDateTime now) {
        final HoldReleaseOutbox outbox = holdReleaseOutboxRepository.findById(outboxId)
                .orElse(null);
        if (outbox == null || outbox.isCompleted()) {
            return;
        }

        try {
            holdManager.release(outbox.getPerformanceId(), outbox.getHoldKey(), outbox.seatIds());
            seatStatusPublisher.publishReleased(outbox.getPerformanceId(), outbox.seatIds());
            outbox.markCompleted(now);
        } catch (final RuntimeException e) {
            outbox.scheduleRetry(now.plus(RETRY_DELAY), e.getMessage());
            log.error("hold release outbox 처리 실패: outboxId={}, holdKey={}", outboxId, outbox.getHoldKey(), e);
        }
    }
}
