package com.ticket.core.support.cursor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticket.core.domain.show.ShowCursor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class CursorCodec {

    private final ObjectMapper objectMapper;

    public String encode(ShowCursor cursor) {
        try {
            String json = objectMapper.writeValueAsString(cursor);
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalArgumentException("cursor encode failed", e);
        }
    }

    public ShowCursor decode(String token) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(token);
            String json = new String(decoded, StandardCharsets.UTF_8);
            return objectMapper.readValue(json, ShowCursor.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("cursor decode failed", e);
        }
    }
}
