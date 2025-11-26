package com.ticket.api.controller.v1;

import com.ticket.api.controller.v1.request.AddMemberRequest;
import com.ticket.api.controller.v1.request.LoginMemberRequest;
import com.ticket.api.controller.v1.response.MemberResponse;
import com.ticket.domain.member.Member;
import com.ticket.domain.member.MemberService;
import com.ticket.support.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/member")
public class MemberController {

    private final MemberService memberService;

    public MemberController(final MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ApiResponse<Long> register(@RequestBody @Valid AddMemberRequest request) {
        return ApiResponse.success(memberService.register(request.toAddMember()));
    }

    @PostMapping("/login")
    public ApiResponse<MemberResponse> login(@RequestBody @Valid LoginMemberRequest request) {
        final Member loginedMember = memberService.login(request.getEmail(), request.getPassword());
        return ApiResponse.success(new MemberResponse(loginedMember.getId(), loginedMember.getEmail().getEmail(), loginedMember.getName()));
    }

    @GetMapping("/{memberId}")
    public ApiResponse<MemberResponse> findById(@PathVariable Long memberId) {
        final Member findMember = memberService.findById(memberId);
        return ApiResponse.success(new MemberResponse(findMember.getId(), findMember.getEmail().getEmail(), findMember.getName()));
    }
}
