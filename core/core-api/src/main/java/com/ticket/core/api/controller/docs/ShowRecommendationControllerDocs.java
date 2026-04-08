package com.ticket.core.api.controller.docs;

import com.ticket.core.config.security.MemberPrincipal;
import com.ticket.core.domain.show.query.GetRecommendedShowsUseCase;
import com.ticket.core.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "공연 추천", description = "회원 취향 기반 공연 추천 API")
public interface ShowRecommendationControllerDocs {

    @Operation(
            summary = "맞춤 공연 추천",
            description = "인증된 회원의 찜 목록과 선호 장르를 기반으로 맞춤 공연을 추천합니다. 찜 이력이 없으면 인기 공연을 반환합니다. GET /api/v1/recommendations/shows"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "추천 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    ApiResponse<GetRecommendedShowsUseCase.Output> getRecommendations(
            @Parameter(hidden = true) MemberPrincipal memberPrincipal,
            @Parameter(description = "추천 개수 (최대 50)", example = "10") int size
    );
}
