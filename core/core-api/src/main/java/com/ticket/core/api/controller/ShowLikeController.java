package com.ticket.core.api.controller;

import com.ticket.core.api.controller.docs.ShowLikeControllerDocs;
import com.ticket.core.api.controller.response.ShowLikeStatusResponse;
import com.ticket.core.api.controller.response.ShowLikeSummaryResponse;
import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.domain.showlike.usecase.AddShowLikeUseCase;
import com.ticket.core.domain.showlike.usecase.GetMyShowLikesUseCase;
import com.ticket.core.domain.showlike.usecase.GetShowLikeStatusUseCase;
import com.ticket.core.domain.showlike.usecase.RemoveShowLikeUseCase;
import com.ticket.core.support.exception.AuthException;
import com.ticket.core.support.exception.ErrorType;
import com.ticket.core.support.response.ApiResponse;
import com.ticket.core.support.response.SliceResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shows")
public class ShowLikeController implements ShowLikeControllerDocs {

    private final AddShowLikeUseCase addShowLikeUseCase;
    private final RemoveShowLikeUseCase removeShowLikeUseCase;
    private final GetShowLikeStatusUseCase getShowLikeStatusUseCase;
    private final GetMyShowLikesUseCase getMyShowLikesUseCase;

    public ShowLikeController(
            final AddShowLikeUseCase addShowLikeUseCase,
            final RemoveShowLikeUseCase removeShowLikeUseCase,
            final GetShowLikeStatusUseCase getShowLikeStatusUseCase,
            final GetMyShowLikesUseCase getMyShowLikesUseCase
    ) {
        this.addShowLikeUseCase = addShowLikeUseCase;
        this.removeShowLikeUseCase = removeShowLikeUseCase;
        this.getShowLikeStatusUseCase = getShowLikeStatusUseCase;
        this.getMyShowLikesUseCase = getMyShowLikesUseCase;
    }

    @Override
    @PostMapping("/{showId}/likes")
    public ApiResponse<ShowLikeStatusResponse> likeShow(
            final MemberPrincipal memberPrincipal,
            @PathVariable final Long showId
    ) {
        final Long memberId = requireMemberId(memberPrincipal);
        final AddShowLikeUseCase.Input input = new AddShowLikeUseCase.Input(memberId, showId);
        final AddShowLikeUseCase.Output output = addShowLikeUseCase.execute(input);
        return ApiResponse.success(output.response());
    }

    @Override
    @DeleteMapping("/{showId}/likes")
    public ApiResponse<ShowLikeStatusResponse> unlikeShow(
            final MemberPrincipal memberPrincipal,
            @PathVariable final Long showId
    ) {
        final Long memberId = requireMemberId(memberPrincipal);
        final RemoveShowLikeUseCase.Input input = new RemoveShowLikeUseCase.Input(memberId, showId);
        final RemoveShowLikeUseCase.Output output = removeShowLikeUseCase.execute(input);
        return ApiResponse.success(output.response());
    }

    @Override
    @GetMapping("/{showId}/likes")
    public ApiResponse<ShowLikeStatusResponse> getLikeStatus(
            final MemberPrincipal memberPrincipal,
            @PathVariable final Long showId
    ) {
        final Long memberId = requireMemberId(memberPrincipal);
        final GetShowLikeStatusUseCase.Input input = new GetShowLikeStatusUseCase.Input(memberId, showId);
        final GetShowLikeStatusUseCase.Output output = getShowLikeStatusUseCase.execute(input);
        return ApiResponse.success(output.response());
    }

    @Override
    @GetMapping("/likes")
    public ApiResponse<SliceResponse<ShowLikeSummaryResponse>> getMyLikes(
            final MemberPrincipal memberPrincipal,
            @RequestParam(required = false) final String cursor,
            @RequestParam(defaultValue = "20") final int size
    ) {
        final Long memberId = requireMemberId(memberPrincipal);
        final GetMyShowLikesUseCase.Input input = new GetMyShowLikesUseCase.Input(memberId, cursor, size);
        final GetMyShowLikesUseCase.Output output = getMyShowLikesUseCase.execute(input);
        return ApiResponse.success(SliceResponse.from(output.shows(), output.nextCursor()));
    }

    private Long requireMemberId(final MemberPrincipal memberPrincipal) {
        if (memberPrincipal == null || memberPrincipal.getMemberId() == null) {
            throw new AuthException(ErrorType.AUTHENTICATION_ERROR);
        }
        return memberPrincipal.getMemberId();
    }
}
