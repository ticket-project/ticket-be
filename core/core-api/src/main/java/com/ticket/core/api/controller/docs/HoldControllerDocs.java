package com.ticket.core.api.controller.docs;

import com.ticket.core.api.controller.request.CreateHoldRequest;
import com.ticket.core.domain.hold.command.usecase.CreateHoldUseCase;
import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "좌석 선점", description = "좌석 HOLD 및 PENDING 주문 생성 API")
public interface HoldControllerDocs {

    @Operation(
            summary = "좌석 HOLD 생성",
            description = """
                    요청한 좌석을 Redis에 HOLD 하고 DB에 PENDING 주문을 생성합니다.
                    한 회원은 같은 회차에 PENDING 주문을 1건만 가질 수 있습니다.
                    좌석 선택(selection)은 UX 보조 상태이고 HOLD 생성의 필수 선행 조건이 아닙니다.
                    다른 사용자가 먼저 선택한 좌석이라도 HOLD 가능한 상태라면 선점이 가능하며, HOLD 성공 시 기존 선택 상태는 정리됩니다.
                    응답 헤더로 생성된 주문의 조회 URI(Location)와 주문 키(X-Order-Key)를 함께 반환합니다.
                    생성된 주문은 GET /api/v1/orders/{orderKey}로 조회할 수 있습니다.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "HOLD 생성 성공",
                    headers = {
                            @Header(name = "Location", description = "생성된 주문 조회 URI", schema = @Schema(type = "string", example = "/api/v1/orders/ORD-3f24c6bc355148f6bf941f0b2f2a6c2b")),
                            @Header(name = "X-Order-Key", description = "생성된 주문 키", schema = @Schema(type = "string", example = "ORD-3f24c6bc355148f6bf941f0b2f2a6c2b"))
                    }
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 선점된 좌석 또는 진행 중인 PENDING 주문 존재",
                    content = @Content(schema = @Schema(implementation = com.ticket.core.support.response.ApiResponse.class))
            )
    })
    ResponseEntity<ApiResponse<CreateHoldUseCase.Output>> createHold(
            @Parameter(description = "회차 ID", example = "1", required = true) Long performanceId,
            CreateHoldRequest request,
            @Parameter(hidden = true) MemberPrincipal memberPrincipal
    );
}
