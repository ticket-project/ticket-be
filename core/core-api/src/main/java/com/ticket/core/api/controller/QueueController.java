package com.ticket.core.api.controller;

import com.ticket.core.api.controller.docs.QueueControllerDocs;
import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.domain.queue.usecase.GetQueueStatusUseCase;
import com.ticket.core.domain.queue.usecase.LeaveQueueUseCase;
import com.ticket.core.domain.queue.usecase.EnterQueueEntryUseCase;
import com.ticket.core.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/queue/performances")
@RequiredArgsConstructor
public class QueueController implements QueueControllerDocs {

    private final EnterQueueEntryUseCase enterQueueEntryUseCase;
    private final GetQueueStatusUseCase getQueueStatusUseCase;
    private final LeaveQueueUseCase leaveQueueUseCase;

    @Override
    @PostMapping("/{performanceId}/enter")
    public ApiResponse<EnterQueueEntryUseCase.Output> enter(
            @PathVariable final Long performanceId,
            final MemberPrincipal memberPrincipal
    ) {
        return ApiResponse.success(enterQueueEntryUseCase.execute(
                new EnterQueueEntryUseCase.Input(performanceId, memberPrincipal.getMemberId())
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
        leaveQueueUseCase.execute(new LeaveQueueUseCase.Input(
                performanceId,
                memberPrincipal.getMemberId(),
                queueEntryId
        ));
        return ApiResponse.success();
    }
}
