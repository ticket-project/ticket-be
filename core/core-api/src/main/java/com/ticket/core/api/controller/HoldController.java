package com.ticket.core.api.controller;

import com.ticket.core.api.controller.request.AddSeatHoldRequest;
import com.ticket.core.api.controller.response.HoldInfoResponse;
import com.ticket.core.domain.hold.Hold;
import com.ticket.core.domain.hold.HoldService;
import com.ticket.core.domain.member.MemberDetails;
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
     * v0 - DB 로만 + scheduler
     */
//    @PostMapping("/v0")
//    public ApiResponse<Void> holdV0(MemberDetails memberDetails, @RequestBody @Valid AddSeatHoldRequest request) {
//        holdServiceV0.hold(memberDetails.getMemberId(), request.toNewSeatHold());
//        return ApiResponse.success();
//    }

    /**
     * v1 - Redisson DistributedLock
     */
    @PostMapping("/v1")
    public ApiResponse<HoldInfoResponse> holdV1(MemberDetails memberDetails, @RequestBody @Valid AddSeatHoldRequest request) {
        final Hold hold = holdService.hold(memberDetails.getMemberId(), request.toNewSeatHold());
        return ApiResponse.success(HoldInfoResponse.from(hold));
    }

}
