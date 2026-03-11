package com.ticket.core.api.controller;

import com.ticket.core.api.controller.docs.HoldControllerDocs;
import com.ticket.core.api.controller.request.CreateHoldRequest;
import com.ticket.core.domain.hold.usecase.CreateHoldUseCase;
import com.ticket.core.domain.member.MemberPrincipal;
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

    private final CreateHoldUseCase createHoldUseCase;

    @Override
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createHold(
            @PathVariable final Long performanceId,
            @Valid @RequestBody final CreateHoldRequest request,
            final MemberPrincipal memberPrincipal
    ) {
        final CreateHoldUseCase.Output output = createHoldUseCase.execute(
                new CreateHoldUseCase.Input(performanceId, request.getSeatIds(), memberPrincipal.getMemberId())
        );
        return ResponseEntity.created(URI.create("/api/v1/orders/" + output.orderId()))
                .body(ApiResponse.success());
    }
}
