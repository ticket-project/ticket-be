package com.ticket.core.api.controller;

import com.ticket.core.api.controller.request.AddSeatHoldRequest;
import com.ticket.core.domain.member.MemberDetails;
import com.ticket.core.domain.seathold.SeatHoldService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SeatHoldController {

    private final SeatHoldService seatHoldService;

    public SeatHoldController(final SeatHoldService seatHoldService) {
        this.seatHoldService = seatHoldService;
    }

    /**
     * v0 선점 - DB 로만 + scheduler
     */
    @PostMapping("/api/v0/hold")
    public void hold(MemberDetails memberDetails, @RequestBody @Valid AddSeatHoldRequest request) {
        seatHoldService.hold(request.toNewSeatHold(memberDetails.getMemberId()));
    }

}
