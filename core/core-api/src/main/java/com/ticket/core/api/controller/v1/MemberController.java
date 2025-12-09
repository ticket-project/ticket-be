package com.ticket.core.api.controller.v1;

import com.ticket.core.api.controller.v1.response.MemberResponse;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberDetails;
import com.ticket.core.domain.member.MemberService;
import com.ticket.core.support.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/member")
public class MemberController {

    private static final Logger log = LoggerFactory.getLogger(MemberController.class);
    private final MemberService memberService;

    public MemberController(final MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public ApiResponse<MemberResponse> findById(MemberDetails memberDetails) {
        log.info("loginMember={}", memberDetails);
        final Member findMember = memberService.findById(memberDetails.getMemberId());
        return ApiResponse.success(new MemberResponse(findMember.getId(), findMember.getEmailValue(), findMember.getName(), findMember.getRole().toString()));
    }
}
