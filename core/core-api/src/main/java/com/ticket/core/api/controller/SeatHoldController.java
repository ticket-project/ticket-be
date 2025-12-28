package com.ticket.core.api.controller;

import com.ticket.core.api.controller.request.AddSeatHoldRequest;
import com.ticket.core.domain.member.MemberDetails;
import com.ticket.core.domain.seathold.SeatHoldService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seathold")
public class SeatHoldController {

    private final SeatHoldService seatHoldService;

    public SeatHoldController(final SeatHoldService seatHoldService) {
        this.seatHoldService = seatHoldService;
    }

    /**
     * v0 선점 - DB 로만 + scheduler
     */
    @PostMapping("/v0")
    public void hold(MemberDetails memberDetails, @RequestBody @Valid AddSeatHoldRequest request) {
        seatHoldService.hold(request.toNewSeatHold(memberDetails.getMemberId()));
    }

}
