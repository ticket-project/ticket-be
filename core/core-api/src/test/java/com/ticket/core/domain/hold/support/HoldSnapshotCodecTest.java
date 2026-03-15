package com.ticket.core.domain.hold.support;

import com.ticket.core.domain.hold.model.HoldSnapshot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class HoldSnapshotCodecTest {

    @Mock
    private JsonMapper jsonMapper;

    @InjectMocks
    private HoldSnapshotCodec codec;

    @Test
    void 스냅샷을_json으로_인코딩한다() throws Exception {
        HoldSnapshot snapshot = createSnapshot();
        when(jsonMapper.writeValueAsString(snapshot)).thenReturn("{\"holdKey\":\"hold-key\"}");

        String result = codec.encode(snapshot);

        assertThat(result).isEqualTo("{\"holdKey\":\"hold-key\"}");
    }

    @Test
    void 인코딩에_실패하면_IllegalStateException을_던진다() throws Exception {
        HoldSnapshot snapshot = createSnapshot();
        when(jsonMapper.writeValueAsString(snapshot)).thenThrow(new RuntimeException("boom"));

        assertThatThrownBy(() -> codec.encode(snapshot))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("encode failed");
    }

    @Test
    void payload를_스냅샷으로_디코딩한다() throws Exception {
        HoldSnapshot snapshot = createSnapshot();
        when(jsonMapper.readValue("{\"holdKey\":\"hold-key\"}", HoldSnapshot.class)).thenReturn(snapshot);

        HoldSnapshot result = codec.decode("{\"holdKey\":\"hold-key\"}");

        assertThat(result).isEqualTo(snapshot);
    }

    @Test
    void 디코딩에_실패하면_IllegalStateException을_던진다() throws Exception {
        when(jsonMapper.readValue("broken", HoldSnapshot.class)).thenThrow(new RuntimeException("boom"));

        assertThatThrownBy(() -> codec.decode("broken"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("decode failed");
    }

    private HoldSnapshot createSnapshot() {
        return new HoldSnapshot(
                "hold-key",
                1L,
                10L,
                List.of(100L, 101L),
                LocalDateTime.of(2026, 3, 15, 12, 30)
        );
    }
}
