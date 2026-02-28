package com.ticket.core.api.controller.docs;

import com.ticket.core.api.controller.response.MemberResponse;
import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "회원(Member)", description = "회원 정보 조회 API")
public interface MemberControllerDocs {

    @Operation(summary = "현재 회원 조회", description = "로그인한 회원의 정보를 조회합니다")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    ApiResponse<MemberResponse> getCurrentMember(
            @Parameter(hidden = true) MemberPrincipal memberPrincipal
    );

    @Operation(summary = "현재 회원 탈퇴", description = "로그인한 회원을 탈퇴 처리합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "탈퇴 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    ApiResponse<Void> withdrawCurrentMember(
            @Parameter(hidden = true) MemberPrincipal memberPrincipal
    );
}
