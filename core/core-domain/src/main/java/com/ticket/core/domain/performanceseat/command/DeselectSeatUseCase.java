package com.ticket.core.domain.performanceseat.command;

import com.ticket.core.domain.performanceseat.support.SeatStatusMessage.SeatAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeselectSeatUseCase {

    private final SeatSelectionService seatSelectionService;
    private final SeatEventPort seatEventPort;

    public record Input(Long performanceId, Long seatId, Long memberId) {}

    public void execute(final Input input) {
        seatSelectionService.deselect(input.performanceId(), input.seatId(), input.memberId());
        seatEventPort.publish(input.performanceId(), input.seatId(), SeatAction.DESELECTED);
    }
}
