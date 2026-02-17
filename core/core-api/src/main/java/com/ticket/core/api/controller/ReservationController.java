package com.ticket.core.api.controller;

import com.ticket.core.api.controller.request.AddReservationRequest;
import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.domain.reservation.ReservationService;
import com.ticket.core.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "예약", description = "좌석 예약 API")
@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "좌석 예약", description = "선점한 좌석을 최종 예약합니다")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "예약 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "좌석 충돌 (이미 예약됨)")
    })
    @PostMapping
    public ApiResponse<Void> reserveV1(MemberPrincipal memberPrincipal, @RequestBody @Valid AddReservationRequest request) {
        reservationService.addReservation(request.toNewReservation(memberPrincipal.getMemberId()));
        return ApiResponse.success(null);
    }

}

