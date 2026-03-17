package com.ticket.core.domain.queue.runtime;

import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.support.exception.AuthException;
import com.ticket.core.support.exception.ErrorType;

import java.time.LocalDateTime;

public record QueueEntryRuntime(
        Long performanceId,
        Long memberId,
        String queueEntryId,
        QueueEntryStatus status,
        Long sequence,
        String queueToken,
        LocalDateTime expiresAt
) {

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

    public boolean requiresTokenValidation() {
        return isAdmitted();
    }

    public QueueEntryAction planReentry(final Long performanceId, final Long memberId) {
        if (!isOwnedBy(performanceId, memberId)) {
            return QueueEntryAction.clearMemberEntry();
        }
        if (isWaiting()) {
            return QueueEntryAction.leaveWaiting(queueEntryId);
        }
        if (isAdmitted() && hasQueueToken()) {
            return QueueEntryAction.leaveAdmittedAndAdvance(queueEntryId, queueToken);
        }
        return QueueEntryAction.clearMemberEntry();
    }

    public QueueEntryAction planLeave(final Long performanceId, final Long memberId) {
        assertOwnedBy(performanceId, memberId);
        if (isWaiting()) {
            return QueueEntryAction.leaveWaiting(queueEntryId);
        }
        if (isAdmitted() && hasQueueToken()) {
            return QueueEntryAction.leaveAdmittedAndAdvance(queueEntryId, queueToken);
        }
        return QueueEntryAction.none();
    }
}
