package com.ticket.core.api.controller;

import com.ticket.core.api.controller.request.AddHoldRequest;
import com.ticket.core.domain.hold.HoldService;
import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "좌석 선점", description = "좌석 임시 선점 API")
@RestController
@RequestMapping("/api/v1/holds")
public class HoldController {
    private final HoldService holdService;

    public HoldController(final HoldService holdService) {
        this.holdService = holdService;
    }

    @Operation(summary = "좌석 선점", description = "선택한 좌석을 임시 선점합니다. 일정 시간이 지나면 자동으로 해제됩니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "좌석 선점 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 선점된 좌석")
    })
    @PostMapping
    public ApiResponse<Long> hold(MemberPrincipal memberPrincipal, @RequestBody @Valid AddHoldRequest request) {
        final Long holdId = holdService.hold(memberPrincipal.getMemberId(), request.toNewHold());
        return ApiResponse.success(holdId);
    }

}

