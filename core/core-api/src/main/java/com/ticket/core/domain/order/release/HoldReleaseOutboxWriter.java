package com.ticket.core.domain.order.release;

import com.ticket.core.domain.order.shared.OrderTerminationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class HoldReleaseOutboxWriter {

    private final HoldReleaseOutboxRepository holdReleaseOutboxRepository;
    private final Clock clock;

    public void append(final OrderTerminationResult result) {
        holdReleaseOutboxRepository.save(HoldReleaseOutbox.create(
                result.performanceId(),
                result.holdKey(),
                result.seatIds(),
                LocalDateTime.now(clock)
        ));
    }
}
