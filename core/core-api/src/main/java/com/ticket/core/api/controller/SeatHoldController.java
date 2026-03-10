package com.ticket.core.api.controller;

import com.ticket.core.api.controller.docs.SeatHoldControllerDocs;
import com.ticket.core.api.controller.request.HoldSeatRequest;
import com.ticket.core.api.controller.response.HoldSeatResponse;
import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.domain.performanceseat.usecase.HoldSeatUseCase;
import com.ticket.core.domain.performanceseat.usecase.ReleaseSeatUseCase;
import com.ticket.core.support.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/performances/{performanceId}/seats")
@RequiredArgsConstructor
public class SeatHoldController implements SeatHoldControllerDocs {

    private final HoldSeatUseCase holdSeatUseCase;
    private final ReleaseSeatUseCase releaseSeatUseCase;

    @Override
    @PostMapping("/hold")
    public ApiResponse<HoldSeatResponse> holdSeats(
            @PathVariable final Long performanceId,
            @RequestBody @Valid final HoldSeatRequest request,
            final MemberPrincipal memberPrincipal
    ) {
        final HoldSeatUseCase.Output output = holdSeatUseCase.execute(new HoldSeatUseCase.Input(
                performanceId, request.seatIds(), memberPrincipal.getMemberId()));

        final HoldSeatResponse response = new HoldSeatResponse(
                output.orderId(),
                output.orderNo(),
                output.totalAmount(),
                output.seats().stream()
                        .map(s -> new HoldSeatResponse.SeatInfo(s.performanceSeatId(), s.price()))
                        .toList()
        );
        return ApiResponse.success(response);
    }

    @Override
    @DeleteMapping("/hold")
    public ApiResponse<Void> releaseSeats(
            @PathVariable final Long performanceId,
            @RequestBody @Valid final HoldSeatRequest request,
            final MemberPrincipal memberPrincipal
    ) {
        releaseSeatUseCase.execute(new ReleaseSeatUseCase.Input(
                performanceId, request.seatIds(), memberPrincipal.getMemberId()));
        return ApiResponse.success();
    }
}

