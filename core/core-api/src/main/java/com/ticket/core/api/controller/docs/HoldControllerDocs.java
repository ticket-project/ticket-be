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

@Tag(name = "Seat Hold", description = "Seat hold and pending order creation API")
public interface HoldControllerDocs {

    @Operation(
            summary = "Create seat hold",
            description = """
                    Creates a Redis-based hold for the selected seats and creates a pending order in the database.
                    The response returns both the order URI through the Location header and the order identifier
                    through the X-Order-Id header so the client can navigate to GET /api/v1/orders/{orderId}.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Hold created",
                    headers = {
                            @Header(name = "Location", description = "Created order resource URI", schema = @Schema(type = "string", example = "/api/v1/orders/1001")),
                            @Header(name = "X-Order-Id", description = "Created order ID", schema = @Schema(type = "string", example = "1001"))
                    }
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Seat already held",
                    content = @Content(schema = @Schema(implementation = com.ticket.core.support.response.ApiResponse.class))
            )
    })
    ResponseEntity<ApiResponse<CreateHoldUseCase.Output>> createHold(
            @Parameter(description = "Performance ID", example = "1", required = true) Long performanceId,
            CreateHoldRequest request,
            @Parameter(hidden = true) MemberPrincipal memberPrincipal
    );
}
