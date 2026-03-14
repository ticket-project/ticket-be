package com.ticket.core.domain.performanceseat.command.usecase;

import com.ticket.core.domain.performanceseat.command.SeatSelectionService;
import com.ticket.core.domain.performanceseat.support.SeatEventPublisher;
import com.ticket.core.domain.performanceseat.support.SeatSelectionAvailabilityValidator;
import com.ticket.core.domain.performanceseat.support.SeatStatusMessage;
import com.ticket.core.domain.performanceseat.support.SeatStatusMessage.SeatAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SelectSeatUseCase {

    private final SeatSelectionService seatSelectionService;
    private final SeatSelectionAvailabilityValidator seatSelectionAvailabilityValidator;
    private final SeatEventPublisher seatEventPublisher;

    public record Input(Long performanceId, Long seatId, Long memberId) {}

    public void execute(final Input input) {
        seatSelectionAvailabilityValidator.validate(input.performanceId(), input.seatId());
        seatSelectionService.select(input.performanceId(), input.seatId(), input.memberId());
        seatEventPublisher.publish(SeatStatusMessage.of(input.performanceId(), input.seatId(), SeatAction.SELECTED));
    }
}
