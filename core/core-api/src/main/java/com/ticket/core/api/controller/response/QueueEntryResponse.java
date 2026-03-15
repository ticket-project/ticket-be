package com.ticket.core.api.controller.response;

import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.domain.queue.usecase.QueueEntryUseCase;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "대기열 진입 결과")
public record QueueEntryResponse(
        @Schema(description = "대기열 상태", example = "WAITING")
        QueueEntryStatus status,

        @Schema(description = "대기열 엔트리 ID", example = "cbe11150-0b4d-4cad-b826-a290863739e6")
        String queueEntryId,

        @Schema(description = "현재 순번", nullable = true, example = "15")
        Long position,

        @Schema(description = "예상 대기 시간(초)", nullable = true, example = "600")
        Long estimatedWaitSeconds,

        @Schema(description = "입장 토큰", nullable = true, example = "1:cbe11150-0b4d-4cad-b826-a290863739e6:0950c326-e01e-40a8-a8cf-41181c9c1944")
        String queueToken,

        @Schema(description = "입장 토큰 만료 시각", nullable = true, example = "2026-03-15T20:30:00")
        LocalDateTime expiresAt
) {

    public static QueueEntryResponse from(final QueueEntryUseCase.Output output) {
        return new QueueEntryResponse(
                output.status(),
                output.queueEntryId(),
                output.position(),
                output.estimatedWaitSeconds(),
                output.queueToken(),
                output.expiresAt()
        );
    }
}
