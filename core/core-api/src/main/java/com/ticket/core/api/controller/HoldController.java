package com.ticket.core.api.controller;

import com.ticket.core.api.controller.request.AddSeatHoldRequest;
import com.ticket.core.domain.member.MemberDetails;
import com.ticket.core.domain.hold.HoldToken;
import com.ticket.core.domain.hold.HoldInfoResponse;
import com.ticket.core.domain.hold.HoldService;
import com.ticket.core.support.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hold")
public class HoldController {

    private final HoldService holdService;

    public HoldController(final HoldService holdService) {
        this.holdService = holdService;
    }

    /**
     * v0 선점 - DB 로만 + scheduler
     */
    @PostMapping("/v0")
    public ApiResponse<HoldInfoResponse> hold(MemberDetails memberDetails, @RequestBody @Valid AddSeatHoldRequest request) {
        final HoldToken holdToken = holdService.hold(request.toNewSeatHold(memberDetails.getMemberId()));
        return ApiResponse.success(HoldInfoResponse.from(holdToken));
    }

}
