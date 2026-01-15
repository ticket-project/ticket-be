package com.ticket.core.domain.hold;

public interface HoldService {
    Long hold(final Long memberId, final NewHold newHold);
}
