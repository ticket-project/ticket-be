package com.ticket.core.api.controller;

import com.ticket.core.api.controller.docs.QueueControllerDocs;
import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.domain.queue.usecase.GetQueueStatusUseCase;
import com.ticket.core.domain.queue.usecase.ExitQueueUseCase;
import com.ticket.core.domain.queue.usecase.JoinQueueUseCase;
import com.ticket.core.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/queue/performances")
@RequiredArgsConstructor
public class QueueController implements QueueControllerDocs {

    private final JoinQueueUseCase joinQueueUseCase;
    private final GetQueueStatusUseCase getQueueStatusUseCase;
    private final ExitQueueUseCase exitQueueUseCase;

    @Override
    @PostMapping("/{performanceId}/enter")
    public ApiResponse<JoinQueueUseCase.Output> enter(
            @PathVariable final Long performanceId,
            final MemberPrincipal memberPrincipal
    ) {
        return ApiResponse.success(joinQueueUseCase.execute(
                new JoinQueueUseCase.Input(performanceId, memberPrincipal.getMemberId())
        ));
    }

    @Override
    @GetMapping("/{performanceId}/status")
    public ApiResponse<GetQueueStatusUseCase.Output> getStatus(
            @PathVariable final Long performanceId,
            @RequestParam final String queueEntryId,
            final MemberPrincipal memberPrincipal
    ) {
        return ApiResponse.success(getQueueStatusUseCase.execute(
                new GetQueueStatusUseCase.Input(performanceId, memberPrincipal.getMemberId(), queueEntryId)
        ));
    }

    @Override
    @PostMapping("/{performanceId}/leave")
    public ApiResponse<Void> leave(
            @PathVariable final Long performanceId,
            @RequestParam final String queueEntryId,
            final MemberPrincipal memberPrincipal
    ) {
        exitQueueUseCase.execute(new ExitQueueUseCase.Input(
                performanceId,
                memberPrincipal.getMemberId(),
                queueEntryId
        ));
        return ApiResponse.success();
    }
}
