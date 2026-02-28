package com.ticket.core.api.controller;

import com.ticket.core.api.controller.docs.HoldControllerDocs;
import com.ticket.core.api.controller.request.AddHoldRequest;
import com.ticket.core.domain.hold.HoldService;
import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.support.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/holds")
@RequiredArgsConstructor
public class HoldController implements HoldControllerDocs {
    private final HoldService holdService;

    @Override
    @PostMapping
    public ApiResponse<Long> hold(MemberPrincipal memberPrincipal, @RequestBody @Valid AddHoldRequest request) {
        final Long holdId = holdService.hold(memberPrincipal.getMemberId(), request.toNewHold());
        return ApiResponse.success(holdId);
    }

}
