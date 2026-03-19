package com.ticket.core.domain.queue.runtime;

import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.support.exception.AuthException;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;

import java.time.LocalDateTime;
import java.util.Objects;

public record QueueEntryRuntime(
        Long performanceId,
        Long memberId,
        String queueEntryId,
        QueueEntryStatus status,
        Long sequence,
        String queueToken,
        LocalDateTime expiresAt
) {

    public QueueEntryRuntime {
        Objects.requireNonNull(performanceId, "performanceId는 null일 수 없습니다.");
        Objects.requireNonNull(memberId, "memberId는 null일 수 없습니다.");
        Objects.requireNonNull(queueEntryId, "queueEntryId는 null일 수 없습니다.");
        Objects.requireNonNull(status, "status는 null일 수 없습니다.");

        if (queueEntryId.isBlank()) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "queueEntryId는 blank일 수 없습니다.");
        }
        if (status == QueueEntryStatus.ADMITTED) {
            if (queueToken == null || queueToken.isBlank()) {
                throw new CoreException(ErrorType.INVALID_REQUEST, "ADMITTED 상태는 queueToken이 반드시 있어야 합니다.");
            }
            if (expiresAt == null) {
                throw new CoreException(ErrorType.INVALID_REQUEST, "ADMITTED 상태는 expiresAt이 반드시 있어야 합니다.");
            }
        }
    }

    public boolean isOwnedBy(final Long performanceId, final Long memberId) {
        return performanceId.equals(this.performanceId) && memberId.equals(this.memberId);
    }

    public void assertOwnedBy(final Long performanceId, final Long memberId) {
        if (!isOwnedBy(performanceId, memberId)) {
            throw new AuthException(ErrorType.AUTHORIZATION_ERROR);
        }
    }

    public boolean isWaiting() {
        return status == QueueEntryStatus.WAITING;
    }

    public boolean isAdmitted() {
        return status == QueueEntryStatus.ADMITTED;
    }

    public boolean hasQueueToken() {
        return queueToken != null && !queueToken.isBlank();
    }
}
