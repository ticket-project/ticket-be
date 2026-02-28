package com.ticket.core.api.controller;

import com.ticket.core.api.controller.docs.ReservationControllerDocs;
import com.ticket.core.api.controller.request.AddReservationRequest;
import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.domain.reservation.ReservationService;
import com.ticket.core.support.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController implements ReservationControllerDocs {

    private final ReservationService reservationService;

    @Override
    @PostMapping
    public ApiResponse<Void> reserveV1(MemberPrincipal memberPrincipal, @RequestBody @Valid AddReservationRequest request) {
        reservationService.addReservation(request.toNewReservation(memberPrincipal.getMemberId()));
        return ApiResponse.success(null);
    }

}
