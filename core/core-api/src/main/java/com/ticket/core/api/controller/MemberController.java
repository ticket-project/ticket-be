package com.ticket.core.api.controller;

import com.ticket.core.api.controller.docs.MemberControllerDocs;
import com.ticket.core.api.controller.response.MemberResponse;
import com.ticket.core.api.controller.response.ShowLikeSummaryResponse;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.domain.member.MemberService;
import com.ticket.core.domain.showlike.usecase.GetMyShowLikesUseCase;
import com.ticket.core.support.response.ApiResponse;
import com.ticket.core.support.response.SliceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController implements MemberControllerDocs {

    private final MemberService memberService;
    private final GetMyShowLikesUseCase getMyShowLikesUseCase;

    @Override
    @GetMapping
    public ApiResponse<MemberResponse> getCurrentMember(MemberPrincipal memberPrincipal) {
        final Member findMember = memberService.findById(memberPrincipal.getMemberId());
        return ApiResponse.success(new MemberResponse(findMember.getId(), findMember.getEmail().getEmail(), findMember.getName(), findMember.getRole().name()));
    }

    @Override
    @DeleteMapping
    public ApiResponse<Void> withdrawCurrentMember(final MemberPrincipal memberPrincipal) {
        memberService.withdraw(memberPrincipal.getMemberId());
        SecurityContextHolder.clearContext();
        return ApiResponse.success();
    }

    @Override
    @GetMapping("/me/likes")
    public ApiResponse<SliceResponse<ShowLikeSummaryResponse>> getMyLikes(
            final MemberPrincipal memberPrincipal,
            @RequestParam(required = false) final String cursor,
            @RequestParam(defaultValue = "20") final int size
    ) {
        final GetMyShowLikesUseCase.Input input = new GetMyShowLikesUseCase.Input(memberPrincipal.getMemberId(), cursor, size);
        final GetMyShowLikesUseCase.Output output = getMyShowLikesUseCase.execute(input);
        return ApiResponse.success(SliceResponse.from(output.shows(), output.nextCursor()));
    }
}
