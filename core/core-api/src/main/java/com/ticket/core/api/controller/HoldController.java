package com.ticket.core.api.controller;

import com.ticket.core.api.controller.request.AddHoldRequest;
import com.ticket.core.domain.hold.HoldService;
import com.ticket.core.domain.member.MemberDetails;
import com.ticket.core.support.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/holds")
public class HoldController {
    private final HoldService holdService;

    public HoldController(final HoldService holdService) {
        this.holdService = holdService;
    }

    @PostMapping("/v1")
    public ApiResponse<Long> holdRedis(MemberDetails memberDetails, @RequestBody @Valid AddHoldRequest request) {
        final Long holdId = holdService.hold(memberDetails.getMemberId(), request.toNewHold());
        return ApiResponse.success(holdId);
    }

}
