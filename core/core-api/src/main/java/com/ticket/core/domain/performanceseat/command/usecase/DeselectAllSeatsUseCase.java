package com.ticket.core.domain.performanceseat.command.usecase;

import com.ticket.core.domain.performanceseat.command.SeatSelectionService;
import com.ticket.core.domain.performanceseat.support.SeatEventPublisher;
import com.ticket.core.domain.performanceseat.support.SeatStatusMessage;
import com.ticket.core.domain.performanceseat.support.SeatStatusMessage.SeatAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeselectAllSeatsUseCase {

    private final SeatSelectionService seatSelectionService;
    private final SeatEventPublisher seatEventPublisher;

    public record Input(Long performanceId, Long memberId) {}

    public void execute(final Input input) {
        for (final Long seatId : seatSelectionService.deselectAll(input.performanceId(), input.memberId())) {
            seatEventPublisher.publish(SeatStatusMessage.of(input.performanceId(), seatId, SeatAction.DESELECTED));
        }
    }
}
