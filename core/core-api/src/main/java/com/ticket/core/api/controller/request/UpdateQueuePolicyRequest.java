package com.ticket.core.api.controller.request;

import com.ticket.core.domain.queue.model.QueueLevel;
import com.ticket.core.domain.queue.model.QueueMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Schema(description = "대기열 정책 수정 요청")
public record UpdateQueuePolicyRequest(
        @Schema(description = "정책 모드", example = "FORCE_ON")
        @NotNull QueueMode queueMode,

        @Schema(description = "대기열 레벨", example = "LEVEL_2")
        @NotNull QueueLevel queueLevel,

        @Schema(description = "동시 입장 허용 인원", example = "500")
        Integer maxActiveUsers,

        @Schema(description = "입장 토큰 TTL(초)", example = "600")
        Integer entryTokenTtlSeconds,

        @Schema(description = "사전 대기열 시작 시각", example = "2026-03-20T19:50:00")
        LocalDateTime preopenQueueStartAt,

        @Schema(description = "대기실 안내 문구", example = "예매 오픈 전 대기열이 운영 중입니다.")
        String waitingRoomMessage,

        @Schema(description = "운영 메모", example = "초고수요 회차")
        String reason
) {
}
