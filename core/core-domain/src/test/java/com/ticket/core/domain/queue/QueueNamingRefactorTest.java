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
        assertPresent("src/main/java/com/ticket/core/domain/queue/infra/RedisQueueTicketStore.java");
        assertPresent("src/main/java/com/ticket/core/domain/queue/command/join/JoinQueueUseCase.java");
        assertPresent("src/main/java/com/ticket/core/domain/queue/command/join/QueueReentryCleaner.java");
        assertPresent("src/main/java/com/ticket/core/domain/queue/command/join/QueueJoinProcessor.java");
        assertPresent("src/main/java/com/ticket/core/domain/queue/query/status/GetQueueStatusUseCase.java");
        assertPresent("src/main/java/com/ticket/core/domain/queue/query/status/QueueStatusReader.java");
        assertPresent("src/main/java/com/ticket/core/domain/queue/command/exit/ExitQueueUseCase.java");
        assertPresent("src/main/java/com/ticket/core/domain/queue/command/exit/QueueExitProcessor.java");
        assertPresent("src/main/java/com/ticket/core/domain/queue/model/QueueEntryId.java");
        assertPresent("src/main/java/com/ticket/core/domain/queue/command/QueueAdmissionAdvancer.java");

        assertMissing("src/main/java/com/ticket/core/domain/queue/runtime/QueueEntryRuntime.java");
        assertMissing("src/main/java/com/ticket/core/domain/queue/runtime/QueueRuntimeStore.java");
        assertMissing("src/main/java/com/ticket/core/domain/queue/runtime/RedisQueueRuntimeStore.java");
        assertMissing("src/main/java/com/ticket/core/domain/queue/usecase/EnterQueueEntryUseCase.java");
        assertMissing("src/main/java/com/ticket/core/domain/queue/usecase/LeaveQueueUseCase.java");
        assertMissing("src/main/java/com/ticket/core/domain/queue/usecase/JoinQueueUseCase.java");
        assertMissing("src/main/java/com/ticket/core/domain/queue/usecase/GetQueueStatusUseCase.java");
        assertMissing("src/main/java/com/ticket/core/domain/queue/usecase/ExitQueueUseCase.java");
        assertMissing("src/main/java/com/ticket/core/domain/queue/usecase/QueueEntryId.java");
        assertMissing("src/main/java/com/ticket/core/domain/queue/join/JoinQueueUseCase.java");
        assertMissing("src/main/java/com/ticket/core/domain/queue/status/GetQueueStatusUseCase.java");
        assertMissing("src/main/java/com/ticket/core/domain/queue/exit/ExitQueueUseCase.java");
        assertMissing("src/main/java/com/ticket/core/domain/queue/shared/QueueEntryId.java");
        assertMissing("src/main/java/com/ticket/core/domain/queue/command/QueueAdvanceProcessor.java");
    }

    private void assertPresent(final String relativePath) {
        assertThat(Files.exists(resolve(relativePath))).isTrue();
    }

    private void assertMissing(final String relativePath) {
        assertThat(Files.exists(resolve(relativePath))).isFalse();
    }

    private Path resolve(final String relativePath) {
        return Path.of(relativePath).normalize();
    }
}
