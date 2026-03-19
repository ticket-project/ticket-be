package com.ticket.core.api.controller;

import com.ticket.core.api.controller.docs.HoldControllerDocs;
import com.ticket.core.api.controller.request.CreateHoldRequest;
import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.domain.order.command.usecase.StartOrderUseCase;
import com.ticket.core.support.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/performances/{performanceId}/holds")
@RequiredArgsConstructor
public class HoldController implements HoldControllerDocs {

    private final StartOrderUseCase startOrderUseCase;

    @Override
    @PostMapping
    public ResponseEntity<ApiResponse<StartOrderUseCase.Output>> createHold(
            @PathVariable final Long performanceId,
            @Valid @RequestBody final CreateHoldRequest request,
            @RequestHeader(value = "X-Queue-Token", required = false) final String queueToken,
            final MemberPrincipal memberPrincipal
    ) {
        final StartOrderUseCase.Input input = new StartOrderUseCase.Input(performanceId, request.getSeatIds(), memberPrincipal.getMemberId());
        final StartOrderUseCase.Output output = startOrderUseCase.execute(input);
        return ResponseEntity.created(URI.create("/api/v1/orders/" + output.orderKey()))
                .header("X-Order-Key", output.orderKey())
                .body(ApiResponse.success(output));
    }
}
