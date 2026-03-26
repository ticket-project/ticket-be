package com.ticket.core.domain.hold.store;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;
import com.ticket.core.domain.hold.model.HoldSnapshot;

@Component
@RequiredArgsConstructor
public class HoldSnapshotCodec {

    private final JsonMapper jsonMapper;

    public String encode(final HoldSnapshot snapshot) {
        try {
            return jsonMapper.writeValueAsString(snapshot);
        } catch (final Exception e) {
            throw new IllegalStateException("hold snapshot encode failed", e);
        }
    }

    public HoldSnapshot decode(final String payload) {
        try {
            return jsonMapper.readValue(payload, HoldSnapshot.class);
        } catch (final Exception e) {
            throw new IllegalStateException("hold snapshot decode failed", e);
        }
    }
}
