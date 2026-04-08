package com.ticket.core.api.controller.docs;

import com.ticket.core.config.security.MemberPrincipal;
import com.ticket.core.domain.showwaitlist.command.JoinShowWaitlistUseCase;
import com.ticket.core.domain.showwaitlist.command.LeaveShowWaitlistUseCase;
import com.ticket.core.domain.showwaitlist.query.GetMyShowWaitlistUseCase;
import com.ticket.core.support.response.ApiResponse;
import com.ticket.core.support.response.SliceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "공연 대기열", description = "공연 취소표/재오픈 대기열 관련 API")
public interface ShowWaitlistControllerDocs {

    @Operation(
            summary = "공연 대기열 등록",
            description = "인증된 회원이 특정 공연의 취소표/재오픈 대기열에 등록합니다. 이미 등록되어 있어도 멱등하게 성공을 반환합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "대기열 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공연 없음")
    })
    ApiResponse<JoinShowWaitlistUseCase.Output> join(
            @Parameter(hidden = true) MemberPrincipal memberPrincipal,
            @Parameter(description = "공연 ID", example = "1", required = true) Long showId
    );

    @Operation(
            summary = "공연 대기열 탈퇴",
            description = "인증된 회원이 공연 대기열에서 탈퇴합니다. 등록되지 않은 상태여도 멱등하게 성공을 반환합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "대기열 탈퇴 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공연 없음")
    })
    ApiResponse<LeaveShowWaitlistUseCase.Output> leave(
            @Parameter(hidden = true) MemberPrincipal memberPrincipal,
            @Parameter(description = "공연 ID", example = "1", required = true) Long showId
    );

    @Operation(
            summary = "내 공연 대기열 목록 조회",
            description = "인증된 회원이 등록한 공연 대기열 목록을 커서 기반 페이지네이션으로 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    ApiResponse<SliceResponse<GetMyShowWaitlistUseCase.ShowWaitlistSummary>> getMyWaitlist(
            @Parameter(hidden = true) MemberPrincipal memberPrincipal,
            @Parameter(description = "커서") String cursor,
            @Parameter(description = "조회 개수", example = "20") int size
    );
}
