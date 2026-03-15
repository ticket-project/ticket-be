package com.ticket.core.domain.queue.runtime;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
class QueueRedisKeyTest {

    @Test
    void waiting_active_sequence_entry_key를_생성한다() {
        assertThat(QueueRedisKey.waiting(10L)).isEqualTo("queue:performance:10:waiting");
        assertThat(QueueRedisKey.active(10L)).isEqualTo("queue:performance:10:active");
        assertThat(QueueRedisKey.sequence(10L)).isEqualTo("queue:performance:10:seq");
        assertThat(QueueRedisKey.entry("qe-10")).isEqualTo("queue:entry:qe-10");
    }

    @Test
    void 토큰과_스토리지키를_생성하고_파싱한다() {
        String token = QueueRedisKey.createToken(10L, "qe-10");
        String storageKey = QueueRedisKey.tokenStorageKey(token);

        assertThat(token).startsWith("10:qe-10:");
        assertThat(storageKey).startsWith("queue:token:");
        assertThat(QueueRedisKey.tryParseToken(token)).isPresent();
        assertThat(QueueRedisKey.tryParseTokenStorageKey(storageKey)).isPresent();
        assertThat(QueueRedisKey.tryParseToken(token).get().performanceId()).isEqualTo(10L);
        assertThat(QueueRedisKey.tryParseToken(token).get().queueEntryId()).isEqualTo("qe-10");
    }

    @Test
    void 잘못된_토큰은_파싱하지_않는다() {
        assertThat(QueueRedisKey.tryParseToken(null)).isEmpty();
        assertThat(QueueRedisKey.tryParseToken(" ")).isEmpty();
        assertThat(QueueRedisKey.tryParseToken("broken-token")).isEmpty();
        assertThat(QueueRedisKey.tryParseTokenStorageKey("broken-key")).isEmpty();
    }
}
