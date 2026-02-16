package com.ticket.core.api.controller;

import com.ticket.core.api.controller.response.MemberResponse;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.domain.member.MemberService;
import com.ticket.core.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원", description = "회원 정보 조회 API")
@RestController
@RequestMapping("/api/v1/members")
public class MemberController {

    private static final Logger log = LoggerFactory.getLogger(MemberController.class);
    private final MemberService memberService;

    public MemberController(final MemberService memberService) {
        this.memberService = memberService;
    }

    @Operation(summary = "현재 회원 조회", description = "로그인한 회원의 정보를 조회합니다")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping
    public ApiResponse<MemberResponse> getCurrentMember(MemberPrincipal memberPrincipal) {
        log.info("loginMember={}", memberPrincipal);
        final Member findMember = memberService.findById(memberPrincipal.getMemberId());
        return ApiResponse.success(new MemberResponse(findMember.getId(), findMember.getEmail().getEmail(), findMember.getName(), findMember.getRole().name()));
    }
}

