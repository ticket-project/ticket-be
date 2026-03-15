package com.ticket.core.api.controller.response;

import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.domain.queue.usecase.GetQueueStatusUseCase;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "대기열 상태 조회 결과")
public record QueueStatusResponse(
        @Schema(description = "대기열 상태", example = "WAITING")
        QueueEntryStatus status,

        @Schema(description = "대기열 엔트리 ID", example = "cbe11150-0b4d-4cad-b826-a290863739e6")
        String queueEntryId,

        @Schema(description = "현재 순번", nullable = true, example = "10")
        Long position,

        @Schema(description = "예상 대기 시간(초)", nullable = true, example = "300")
        Long estimatedWaitSeconds,

        @Schema(description = "입장 토큰", nullable = true)
        String queueToken,

        @Schema(description = "입장 토큰 만료 시각", nullable = true)
        LocalDateTime expiresAt
) {

    public static QueueStatusResponse from(final GetQueueStatusUseCase.Output output) {
        return new QueueStatusResponse(
                output.status(),
                output.queueEntryId(),
                output.position(),
                output.estimatedWaitSeconds(),
                output.queueToken(),
                output.expiresAt()
        );
    }
}
