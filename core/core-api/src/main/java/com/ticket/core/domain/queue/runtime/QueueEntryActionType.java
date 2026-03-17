package com.ticket.core.domain.queue.runtime;

public enum QueueEntryActionType {
    NONE,
    CLEAR_MEMBER_ENTRY,
    LEAVE_WAITING,
    LEAVE_ADMITTED_AND_ADVANCE
}
