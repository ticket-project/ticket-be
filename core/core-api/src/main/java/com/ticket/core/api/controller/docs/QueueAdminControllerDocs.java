package com.ticket.core.api.controller.docs;

import com.ticket.core.api.controller.request.UpdateQueuePolicyRequest;
import com.ticket.core.api.controller.response.QueuePolicyResponse;
import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "대기열 관리", description = "운영자용 회차 대기열 정책 관리 API")
public interface QueueAdminControllerDocs {

    @Operation(summary = "회차 대기열 정책 조회")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ApiResponse<QueuePolicyResponse> getPolicy(
            @Parameter(description = "회차 ID", example = "1", required = true) Long performanceId,
            @Parameter(hidden = true) MemberPrincipal memberPrincipal
    );

    @Operation(summary = "회차 대기열 정책 수정")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공")
    })
    ApiResponse<QueuePolicyResponse> updatePolicy(
            @Parameter(description = "회차 ID", example = "1", required = true) Long performanceId,
            UpdateQueuePolicyRequest request,
            @Parameter(hidden = true) MemberPrincipal memberPrincipal
    );
}
