package com.ticket.core.domain.hold;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class HoldSnapshotCodec {

    private final JsonMapper jsonMapper;

    public String encode(final HoldRedisService.HoldSnapshot snapshot) {
        try {
            return jsonMapper.writeValueAsString(snapshot);
        } catch (final Exception e) {
            throw new IllegalStateException("hold snapshot encode failed", e);
        }
    }

    public HoldRedisService.HoldSnapshot decode(final String payload) {
        try {
            return jsonMapper.readValue(payload, HoldRedisService.HoldSnapshot.class);
        } catch (final Exception e) {
            throw new IllegalStateException("hold snapshot decode failed", e);
        }
    }
}
