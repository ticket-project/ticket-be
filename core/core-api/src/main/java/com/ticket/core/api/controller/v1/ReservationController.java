package com.ticket.core.api.controller.v1;

import com.ticket.core.api.controller.v1.request.ReserveSeatsRequest;
import com.ticket.core.domain.member.Member;
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
     * 예매
     * @param request
     */
    @PostMapping
    public void reserve(Member member, @RequestBody @Valid ReserveSeatsRequest request) {
        reservationService.reserve(member, request.toAddPerformanceSeat(member));
    }
}
