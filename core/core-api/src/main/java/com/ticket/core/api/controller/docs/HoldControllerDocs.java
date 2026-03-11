package com.ticket.core.api.controller.docs;

import com.ticket.core.api.controller.request.CreateHoldRequest;
import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
                    좌석 선택 화면에서 '결제하러 가기'를 누를 때 호출합니다.
                    선택한 좌석들을 Redis에 HOLD 하고 DB에 PENDING 주문을 생성합니다.
                    다중 좌석은 전부 성공하거나 전부 실패합니다.
                    응답 Location 헤더로 받은 주문 URI로 주문/결제 화면으로 이동한 뒤 GET /api/v1/orders/{orderId}를 호출하면 됩니다.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "HOLD 생성 성공",
                    headers = {
                            @Header(name = "Location", description = "생성된 주문 조회 URI", schema = @Schema(type = "string", example = "/api/v1/orders/1001"))
                    }
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 선점된 좌석",
                    content = @Content(schema = @Schema(implementation = com.ticket.core.support.response.ApiResponse.class)))
    })
    ResponseEntity<ApiResponse<Void>> createHold(
            @Parameter(description = "회차 ID", example = "1", required = true) Long performanceId,
            CreateHoldRequest request,
            @Parameter(hidden = true) MemberPrincipal memberPrincipal
    );
}
