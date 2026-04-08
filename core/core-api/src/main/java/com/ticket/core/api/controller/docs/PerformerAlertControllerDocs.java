package com.ticket.core.api.controller.docs;

import com.ticket.core.config.security.MemberPrincipal;
import com.ticket.core.domain.performeralert.command.SubscribePerformerAlertUseCase;
import com.ticket.core.domain.performeralert.command.UnsubscribePerformerAlertUseCase;
import com.ticket.core.domain.performeralert.query.GetMyPerformerAlertsUseCase;
import com.ticket.core.support.response.ApiResponse;
import com.ticket.core.support.response.SliceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "공연자 알림 구독", description = "공연자 알림 구독 관련 API")
public interface PerformerAlertControllerDocs {

    @Operation(
            summary = "공연자 알림 구독",
            description = "인증된 회원이 특정 공연자의 티켓 오픈 알림을 구독합니다. 이미 구독 중이어도 멱등하게 성공을 반환합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "구독 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공연자 없음")
    })
    ApiResponse<SubscribePerformerAlertUseCase.Output> subscribe(
            @Parameter(hidden = true) MemberPrincipal memberPrincipal,
            @Parameter(description = "공연자 ID", example = "1", required = true) Long performerId
    );

    @Operation(
            summary = "공연자 알림 구독 취소",
            description = "인증된 회원의 공연자 알림 구독을 취소합니다. 구독하지 않은 상태여도 멱등하게 성공을 반환합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "구독 취소 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공연자 없음")
    })
    ApiResponse<UnsubscribePerformerAlertUseCase.Output> unsubscribe(
            @Parameter(hidden = true) MemberPrincipal memberPrincipal,
            @Parameter(description = "공연자 ID", example = "1", required = true) Long performerId
    );

    @Operation(
            summary = "내 공연자 알림 구독 목록 조회",
            description = "인증된 회원이 구독 중인 공연자 알림 목록을 커서 기반 페이지네이션으로 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    ApiResponse<SliceResponse<GetMyPerformerAlertsUseCase.PerformerAlertSummary>> getMyAlerts(
            @Parameter(hidden = true) MemberPrincipal memberPrincipal,
            @Parameter(description = "커서") String cursor,
            @Parameter(description = "조회 개수", example = "20") int size
    );
}
