package com.ticket.core.api.controller;

import com.ticket.core.api.controller.docs.QueueControllerDocs;
import com.ticket.core.api.controller.response.QueueEntryResponse;
import com.ticket.core.api.controller.response.QueueStatusResponse;
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
    public ApiResponse<QueueEntryResponse> enter(@PathVariable final Long performanceId) {
        final QueueEntryUseCase.Output output = queueEntryUseCase.execute(new QueueEntryUseCase.Input(performanceId));
        return ApiResponse.success(QueueEntryResponse.from(output));
    }

    @Override
    @GetMapping("/{performanceId}/status")
    public ApiResponse<QueueStatusResponse> getStatus(
            @PathVariable final Long performanceId,
            @RequestParam final String queueEntryId
    ) {
        final GetQueueStatusUseCase.Output output = getQueueStatusUseCase.execute(
                new GetQueueStatusUseCase.Input(performanceId, queueEntryId)
        );
        return ApiResponse.success(QueueStatusResponse.from(output));
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
