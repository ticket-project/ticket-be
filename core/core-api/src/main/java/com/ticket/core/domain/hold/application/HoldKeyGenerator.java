package com.ticket.core.domain.hold.application;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class HoldKeyGenerator {

    public String generate() {
        return "HOLD-" + UUID.randomUUID().toString().replace("-", "");
    }
}