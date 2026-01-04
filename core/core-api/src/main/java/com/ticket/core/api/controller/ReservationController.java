package com.ticket.core.api.controller;

import com.ticket.core.api.controller.request.AddReservationRequest;
import com.ticket.core.domain.member.MemberDetails;
import com.ticket.core.domain.reservation.ReservationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservation")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(@Qualifier("reservationServiceV1") final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    /**
     * v0 락 사용 X. db의 update 시 발생하는 암시적 락에 의해 오버셀 방지
     */
    @PostMapping("/v0")
    public void reserveV0(MemberDetails memberDetails, @RequestBody @Valid AddReservationRequest request) {
        reservationService.addReservation(request.toNewReservation(memberDetails.getMemberId()));
    }

    /**
     * v1 비관적 락 사용.
     */
    @PostMapping("/v1")
    public void reserveV1(MemberDetails memberDetails, @RequestBody @Valid AddReservationRequest request) {
        reservationService.addReservation(request.toNewReservation(memberDetails.getMemberId()));
    }

}
