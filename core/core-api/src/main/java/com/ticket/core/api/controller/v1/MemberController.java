package com.ticket.core.api.controller.v1;

import com.ticket.core.api.controller.v1.request.AddMemberRequest;
import com.ticket.core.api.controller.v1.request.LoginMemberRequest;
import com.ticket.core.api.controller.v1.response.MemberResponse;
import com.ticket.core.domain.auth.SessionConst;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.LoginMember;
import com.ticket.core.domain.member.MemberService;
import com.ticket.core.support.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/member")
public class MemberController {

    private static final Logger log = LoggerFactory.getLogger(MemberController.class);
    private final MemberService memberService;

    public MemberController(final MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ApiResponse<Long> register(@RequestBody @Valid AddMemberRequest request) {
        return ApiResponse.success(memberService.register(request.toAddMember()));
    }

    @PostMapping("/login")
    public ApiResponse<MemberResponse> login(@RequestBody @Valid LoginMemberRequest request, HttpServletRequest httpRequest) {
        final Member loginedMember = memberService.login(request.getEmail(), request.getPassword());

        final HttpSession session = httpRequest.getSession();
        session.setAttribute(SessionConst.LOGIN_MEMBER, loginedMember);
        return ApiResponse.success(new MemberResponse(loginedMember.getId(), loginedMember.getEmail().getEmail(), loginedMember.getName()));
    }

    @GetMapping
    public ApiResponse<MemberResponse> findById(LoginMember loginMember) {
        log.info("loginMember={}", loginMember);
        final Member findMember = memberService.findById(loginMember.getMemberId());
        return ApiResponse.success(new MemberResponse(findMember.getId(), findMember.getEmail().getEmail(), findMember.getName()));
    }
}
