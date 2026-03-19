package com.ticket.core.domain.queue;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
class QueueSupportCleanupTest {

    @Test
    void 미사용_queue_support_클래스는_제거한다() {
        assertMissing("src/main/java/com/ticket/core/domain/queue/support/QueueWaitTimeEstimator.java");
        assertMissing("src/main/java/com/ticket/core/domain/queue/support/QueueTokenGatekeeper.java");
        assertMissing("src/test/java/com/ticket/core/domain/queue/support/QueueWaitTimeEstimatorTest.java");
        assertMissing("src/test/java/com/ticket/core/domain/queue/support/QueueTokenGatekeeperTest.java");
    }

    private void assertMissing(final String relativePath) {
        assertThat(Files.exists(Path.of(relativePath))).isFalse();
    }
}
