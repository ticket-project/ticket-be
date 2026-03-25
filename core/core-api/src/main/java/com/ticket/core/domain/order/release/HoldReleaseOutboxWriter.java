package com.ticket.core.domain.order.release;

import com.ticket.core.domain.order.shared.OrderTerminationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class HoldReleaseOutboxWriter {

    private final HoldReleaseOutboxRepository holdReleaseOutboxRepository;

    public Long append(final OrderTerminationResult result) {
        final HoldReleaseOutbox outbox = holdReleaseOutboxRepository.save(HoldReleaseOutbox.create(
                result.performanceId(),
                result.holdKey(),
                result.seatIds(),
                LocalDateTime.now()
        ));
        return outbox.getId();
    }
}
