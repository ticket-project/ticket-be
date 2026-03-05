package com.ticket.core.domain.performanceseat.usecase;

import com.ticket.core.domain.performanceseat.SeatEventPublisher;
import com.ticket.core.domain.performanceseat.SeatSelectionService;
import com.ticket.core.domain.performanceseat.SeatStatusMessage;
import com.ticket.core.domain.performanceseat.SeatStatusMessage.SeatAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeselectSeatUseCase {

    private final SeatSelectionService seatSelectionService;
    private final SeatEventPublisher seatEventPublisher;

    public record Input(Long performanceId, Long seatId, Long memberId) {}

    public void execute(final Input input) {
        seatSelectionService.deselect(input.performanceId(), input.seatId(), input.memberId());
        seatEventPublisher.publish(SeatStatusMessage.of(input.performanceId(), input.seatId(), SeatAction.DESELECTED));
    }
}
