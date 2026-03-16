package com.ticket.core.api.controller;

import com.ticket.core.api.controller.docs.QueueControllerDocs;
import com.ticket.core.domain.queue.usecase.GetQueueStatusUseCase;
import com.ticket.core.domain.queue.usecase.LeaveQueueUseCase;
import com.ticket.core.domain.queue.usecase.QueueEntryUseCase;
import com.ticket.core.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/queue/performances")
@RequiredArgsConstructor
public class QueueController implements QueueControllerDocs {

    private final QueueEntryUseCase queueEntryUseCase;
    private final GetQueueStatusUseCase getQueueStatusUseCase;
    private final LeaveQueueUseCase leaveQueueUseCase;

    @Override
    @PostMapping("/{performanceId}/enter")
    public ApiResponse<QueueEntryUseCase.Output> enter(@PathVariable final Long performanceId) {
        return ApiResponse.success(queueEntryUseCase.execute(new QueueEntryUseCase.Input(performanceId)));
    }

    @Override
    @GetMapping("/{performanceId}/status")
    public ApiResponse<GetQueueStatusUseCase.Output> getStatus(
            @PathVariable final Long performanceId,
            @RequestParam final String queueEntryId
    ) {
        return ApiResponse.success(getQueueStatusUseCase.execute(
                new GetQueueStatusUseCase.Input(performanceId, queueEntryId)
        ));
    }

    @Override
    @PostMapping("/{performanceId}/leave")
    public ApiResponse<Void> leave(
            @PathVariable final Long performanceId,
            @RequestParam final String queueEntryId
    ) {
        leaveQueueUseCase.execute(new LeaveQueueUseCase.Input(performanceId, queueEntryId));
        return ApiResponse.success();
    }
}
