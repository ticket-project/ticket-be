package com.ticket.core.api.controller;

import com.ticket.core.api.controller.docs.ShowWaitlistControllerDocs;
import com.ticket.core.config.security.MemberPrincipal;
import com.ticket.core.domain.showwaitlist.command.JoinShowWaitlistUseCase;
import com.ticket.core.domain.showwaitlist.command.LeaveShowWaitlistUseCase;
import com.ticket.core.domain.showwaitlist.query.GetMyShowWaitlistUseCase;
import com.ticket.core.support.exception.AuthException;
import com.ticket.core.support.exception.ErrorType;
import com.ticket.core.support.response.ApiResponse;
import com.ticket.core.support.response.SliceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/waitlists")
@RequiredArgsConstructor
public class ShowWaitlistController implements ShowWaitlistControllerDocs {

    private final JoinShowWaitlistUseCase joinShowWaitlistUseCase;
    private final LeaveShowWaitlistUseCase leaveShowWaitlistUseCase;
    private final GetMyShowWaitlistUseCase getMyShowWaitlistUseCase;

    @Override
    @PostMapping("/shows/{showId}")
    public ApiResponse<JoinShowWaitlistUseCase.Output> join(
            final MemberPrincipal memberPrincipal,
            @PathVariable final Long showId
    ) {
        final Long memberId = requireMemberId(memberPrincipal);
        final JoinShowWaitlistUseCase.Input input = new JoinShowWaitlistUseCase.Input(memberId, showId);
        return ApiResponse.success(joinShowWaitlistUseCase.execute(input));
    }

    @Override
    @DeleteMapping("/shows/{showId}")
    public ApiResponse<LeaveShowWaitlistUseCase.Output> leave(
            final MemberPrincipal memberPrincipal,
            @PathVariable final Long showId
    ) {
        final Long memberId = requireMemberId(memberPrincipal);
        final LeaveShowWaitlistUseCase.Input input = new LeaveShowWaitlistUseCase.Input(memberId, showId);
        return ApiResponse.success(leaveShowWaitlistUseCase.execute(input));
    }

    @Override
    @GetMapping("/me")
    public ApiResponse<SliceResponse<GetMyShowWaitlistUseCase.ShowWaitlistSummary>> getMyWaitlist(
            final MemberPrincipal memberPrincipal,
            @RequestParam(required = false) final String cursor,
            @RequestParam(defaultValue = "20") final int size
    ) {
        final Long memberId = requireMemberId(memberPrincipal);
        final GetMyShowWaitlistUseCase.Input input = new GetMyShowWaitlistUseCase.Input(memberId, cursor, size);
        final GetMyShowWaitlistUseCase.Output output = getMyShowWaitlistUseCase.execute(input);
        return ApiResponse.success(SliceResponse.from(output.shows(), output.nextCursor()));
    }

    private Long requireMemberId(final MemberPrincipal memberPrincipal) {
        if (memberPrincipal == null || memberPrincipal.getMemberId() == null) {
            throw new AuthException(ErrorType.AUTHENTICATION_ERROR);
        }
        return memberPrincipal.getMemberId();
    }
}
