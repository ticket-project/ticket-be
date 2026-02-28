package com.ticket.core.api.controller;

import com.ticket.core.api.controller.docs.MemberControllerDocs;
import com.ticket.core.api.controller.response.MemberResponse;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.domain.member.MemberService;
import com.ticket.core.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController implements MemberControllerDocs {

    private final MemberService memberService;

    @Override
    @GetMapping
    public ApiResponse<MemberResponse> getCurrentMember(MemberPrincipal memberPrincipal) {
        log.info("loginMember={}", memberPrincipal);
        final Member findMember = memberService.findById(memberPrincipal.getMemberId());
        return ApiResponse.success(new MemberResponse(findMember.getId(), findMember.getEmail().getEmail(), findMember.getName(), findMember.getRole().name()));
    }

    @Override
    @DeleteMapping
    public ApiResponse<Void> withdrawCurrentMember(final MemberPrincipal memberPrincipal) {
        log.info("withdrawMember memberId={}", memberPrincipal.getMemberId());
        memberService.withdraw(memberPrincipal.getMemberId());
        SecurityContextHolder.clearContext();
        return ApiResponse.success();
    }
}
