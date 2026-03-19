package com.ticket.core.domain.queue;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
class QueueNamingRefactorTest {

    @Test
    void queue_핵심_타입은_비즈니스_의미_중심_이름을_사용한다() {
        assertPresent("src/main/java/com/ticket/core/domain/queue/runtime/QueueTicket.java");
        assertPresent("src/main/java/com/ticket/core/domain/queue/runtime/QueueTicketStore.java");
        assertPresent("src/main/java/com/ticket/core/domain/queue/runtime/RedisQueueTicketStore.java");
        assertPresent("src/main/java/com/ticket/core/domain/queue/usecase/JoinQueueUseCase.java");
        assertPresent("src/main/java/com/ticket/core/domain/queue/usecase/ExitQueueUseCase.java");
        assertPresent("src/main/java/com/ticket/core/domain/queue/command/QueueAdmissionProcessor.java");

        assertMissing("src/main/java/com/ticket/core/domain/queue/runtime/QueueEntryRuntime.java");
        assertMissing("src/main/java/com/ticket/core/domain/queue/runtime/QueueRuntimeStore.java");
        assertMissing("src/main/java/com/ticket/core/domain/queue/runtime/RedisQueueRuntimeStore.java");
        assertMissing("src/main/java/com/ticket/core/domain/queue/usecase/EnterQueueEntryUseCase.java");
        assertMissing("src/main/java/com/ticket/core/domain/queue/usecase/LeaveQueueUseCase.java");
        assertMissing("src/main/java/com/ticket/core/domain/queue/command/QueueAdvanceProcessor.java");
    }

    private void assertPresent(final String relativePath) {
        assertThat(Files.exists(resolve(relativePath))).isTrue();
    }

    private void assertMissing(final String relativePath) {
        assertThat(Files.exists(resolve(relativePath))).isFalse();
    }

    private Path resolve(final String relativePath) {
        return Path.of(relativePath);
    }
}
