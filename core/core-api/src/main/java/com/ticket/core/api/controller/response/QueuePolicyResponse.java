package com.ticket.core.api.controller.response;

import com.ticket.core.domain.queue.model.QueueLevel;
import com.ticket.core.domain.queue.model.QueueMode;
import com.ticket.core.domain.queue.support.QueuePolicyAdminService;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "대기열 운영 정책 응답")
public record QueuePolicyResponse(
        Long performanceId,
        QueueMode queueMode,
        QueueLevel queueLevel,
        Integer maxActiveUsers,
        Integer entryTokenTtlSeconds,
        LocalDateTime preopenQueueStartAt,
        String waitingRoomMessage,
        String reason
) {

    public static QueuePolicyResponse from(final QueuePolicyAdminService.PolicyDetail detail) {
        return new QueuePolicyResponse(
                detail.performanceId(),
                detail.queueMode(),
                detail.queueLevel(),
                detail.maxActiveUsers(),
                detail.entryTokenTtlSeconds(),
                detail.preopenQueueStartAt(),
                detail.waitingRoomMessage(),
                detail.reason()
        );
    }
}
