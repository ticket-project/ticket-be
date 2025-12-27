package com.ticket.core.api.controller.v1;

import com.ticket.core.api.controller.v1.request.AddReservationRequest;
import com.ticket.core.domain.member.MemberDetails;
import com.ticket.core.domain.reservation.ReservationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    /**
     * v0 락 사용 X. db의 update 시 발생하는 암시적 락에 의해 오버셀 방지
     */
    @PostMapping("/api/v0/reserve")
    public void reserveV0(MemberDetails memberDetails, @RequestBody @Valid AddReservationRequest request) {
        reservationService.addReservation(request.toNewReservation(memberDetails.getMemberId()));
    }

    /**
     * v1 비관적 락 사용.
     */
    @PostMapping("/api/v1/reserve")
    public void reserveV1(MemberDetails memberDetails, @RequestBody @Valid AddReservationRequest request) {
        reservationService.addReservation(request.toNewReservation(memberDetails.getMemberId()));
    }

    /**
     * v1 선점 - DB 로만 + scheduler
     */
    @PostMapping("/api/v2/hold")
    public void reserveV2(MemberDetails memberDetails, @RequestBody @Valid AddReservationRequest request) {
        reservationService.addReservation(request.toNewReservation(memberDetails.getMemberId()));
    }

}
