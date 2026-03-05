package com.ticket.core.api.controller;

import com.ticket.core.api.controller.docs.SeatSelectionControllerDocs;
import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.domain.performanceseat.usecase.DeselectSeatUseCase;
import com.ticket.core.domain.performanceseat.usecase.SelectSeatUseCase;
import com.ticket.core.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/performances/{performanceId}/seats")
@RequiredArgsConstructor
public class SeatSelectionController implements SeatSelectionControllerDocs {

    private final SelectSeatUseCase selectSeatUseCase;
    private final DeselectSeatUseCase deselectSeatUseCase;

    @Override
    @PostMapping("/{seatId}/select")
    public ApiResponse<Void> selectSeat(
            @PathVariable final Long performanceId,
            @PathVariable final Long seatId,
            final MemberPrincipal memberPrincipal
    ) {
        selectSeatUseCase.execute(new SelectSeatUseCase.Input(performanceId, seatId, memberPrincipal.getMemberId()));
        return ApiResponse.success();
    }

    @Override
    @DeleteMapping("/{seatId}/select")
    public ApiResponse<Void> deselectSeat(
            @PathVariable final Long performanceId,
            @PathVariable final Long seatId,
            final MemberPrincipal memberPrincipal
    ) {
        deselectSeatUseCase.execute(new DeselectSeatUseCase.Input(performanceId, seatId, memberPrincipal.getMemberId()));
        return ApiResponse.success();
    }
}
