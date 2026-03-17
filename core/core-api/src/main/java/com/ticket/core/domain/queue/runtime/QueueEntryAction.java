package com.ticket.core.domain.queue.runtime;

public record QueueEntryAction(
        QueueEntryActionType type,
        String queueEntryId,
        String queueToken
) {

    public static QueueEntryAction none() {
        return new QueueEntryAction(QueueEntryActionType.NONE, null, null);
    }

    public static QueueEntryAction clearMemberEntry() {
        return new QueueEntryAction(QueueEntryActionType.CLEAR_MEMBER_ENTRY, null, null);
    }

    public static QueueEntryAction leaveWaiting(final String queueEntryId) {
        return new QueueEntryAction(QueueEntryActionType.LEAVE_WAITING, queueEntryId, null);
    }

    public static QueueEntryAction leaveAdmittedAndAdvance(final String queueEntryId, final String queueToken) {
        return new QueueEntryAction(QueueEntryActionType.LEAVE_ADMITTED_AND_ADVANCE, queueEntryId, queueToken);
    }
}
