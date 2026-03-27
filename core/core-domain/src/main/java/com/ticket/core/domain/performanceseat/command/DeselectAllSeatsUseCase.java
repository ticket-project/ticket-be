package com.ticket.core.domain.performanceseat.command;

import com.ticket.core.domain.member.query.MemberFinder;
import com.ticket.core.domain.performanceseat.support.SeatEventPublisher;
import com.ticket.core.domain.performanceseat.support.SeatStatusMessage.SeatAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeselectAllSeatsUseCase {

    private final MemberFinder memberFinder;
    private final SeatSelectionService seatSelectionService;
    private final SeatEventPublisher seatEventPublisher;

    public record Input(Long performanceId, Long memberId) {}

    public void execute(final Input input) {
        memberFinder.findActiveMemberById(input.memberId());
        final DeselectedSeatIds seatIds = seatSelectionService.deselectAll(input.performanceId(), input.memberId());
        seatIds.forEach(seatId -> seatEventPublisher.publish(input.performanceId(), seatId, SeatAction.DESELECTED));
    }
}
