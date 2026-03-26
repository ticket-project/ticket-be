package com.ticket.core.support.random;

import java.util.UUID;

@FunctionalInterface
public interface UuidSupplier {
    UUID get();
}
