package com.ticket.core.api.controller;

import com.ticket.core.api.controller.docs.ShowRecommendationControllerDocs;
import com.ticket.core.config.security.MemberPrincipal;
import com.ticket.core.domain.show.query.GetRecommendedShowsUseCase;
import com.ticket.core.support.exception.AuthException;
import com.ticket.core.support.exception.ErrorType;
import com.ticket.core.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class ShowRecommendationController implements ShowRecommendationControllerDocs {

    private final GetRecommendedShowsUseCase getRecommendedShowsUseCase;

    @Override
    @GetMapping("/shows")
    public ApiResponse<GetRecommendedShowsUseCase.Output> getRecommendations(
            final MemberPrincipal memberPrincipal,
            @RequestParam(defaultValue = "10") final int size
    ) {
        final Long memberId = requireMemberId(memberPrincipal);
        final GetRecommendedShowsUseCase.Input input = new GetRecommendedShowsUseCase.Input(memberId, size);
        return ApiResponse.success(getRecommendedShowsUseCase.execute(input));
    }

    private Long requireMemberId(final MemberPrincipal memberPrincipal) {
        if (memberPrincipal == null || memberPrincipal.getMemberId() == null) {
            throw new AuthException(ErrorType.AUTHENTICATION_ERROR);
        }
        return memberPrincipal.getMemberId();
    }
}
