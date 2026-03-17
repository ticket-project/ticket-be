package com.ticket.core.domain.queue.runtime;

import com.ticket.core.domain.queue.command.QueueAdvanceProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QueueEntryLifecycleService {

    private final QueueRuntimeStore queueRuntimeStore;
    private final QueueAdvanceProcessor queueAdvanceProcessor;

    public void cleanupForReentry(final Long performanceId, final Long memberId) {
        final String existingQueueEntryId = queueRuntimeStore.findMemberEntryId(performanceId, memberId).orElse(null);
        if (existingQueueEntryId == null) {
            return;
        }

        final QueueEntryRuntime entry = queueRuntimeStore.findEntry(existingQueueEntryId).orElse(null);
        if (entry == null) {
            execute(performanceId, memberId, QueueEntryAction.clearMemberEntry());
            return;
        }
        execute(performanceId, memberId, entry.planReentry(performanceId, memberId));
    }

    public void leave(final Long performanceId, final Long memberId, final QueueEntryRuntime entry) {
        execute(performanceId, memberId, entry.planLeave(performanceId, memberId));
    }

    private void execute(final Long performanceId, final Long memberId, final QueueEntryAction action) {
        switch (action.type()) {
            case NONE -> {
                return;
            }
            case CLEAR_MEMBER_ENTRY -> queueRuntimeStore.clearMemberEntry(performanceId, memberId);
            case LEAVE_WAITING -> queueRuntimeStore.leaveWaiting(performanceId, action.queueEntryId());
            case LEAVE_ADMITTED_AND_ADVANCE -> {
                queueRuntimeStore.leaveAdmitted(performanceId, action.queueEntryId(), action.queueToken());
                queueAdvanceProcessor.advance(performanceId);
            }
        }
    }
}
