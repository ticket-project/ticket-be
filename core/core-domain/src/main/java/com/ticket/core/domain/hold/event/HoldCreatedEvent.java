package com.ticket.core.domain.hold.event;

import com.ticket.core.domain.hold.model.HoldSnapshot;

public record HoldCreatedEvent(HoldSnapshot snapshot) {
}
