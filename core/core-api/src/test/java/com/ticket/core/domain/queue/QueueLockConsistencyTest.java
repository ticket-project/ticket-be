package com.ticket.core.domain.queue;

import com.ticket.core.aop.DistributedLock;
import com.ticket.core.domain.queue.command.QueueAdmissionProcessor;
import com.ticket.core.domain.queue.usecase.ExitQueueUseCase;
import com.ticket.core.domain.queue.usecase.JoinQueueUseCase;
import com.ticket.core.domain.queue.usecase.QueueEntryId;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
class QueueLockConsistencyTest {

    @Test
    void queue_상태전이는_공연기준_하나의_락_네임스페이스를_쓴다() throws NoSuchMethodException {
        assertThat(lockOf(JoinQueueUseCase.class, "execute", JoinQueueUseCase.Input.class).prefix()).isEqualTo("queue");
        assertThat(lockOf(ExitQueueUseCase.class, "execute", ExitQueueUseCase.Input.class).prefix()).isEqualTo("queue");
        assertThat(lockOf(QueueAdmissionProcessor.class, "advance", Long.class).prefix()).isEqualTo("queue");
        assertThat(lockOf(QueueAdmissionProcessor.class, "handleTokenExpired", Long.class, QueueEntryId.class, String.class).prefix())
                .isEqualTo("queue");
    }

    private DistributedLock lockOf(final Class<?> type, final String methodName, final Class<?>... parameterTypes)
            throws NoSuchMethodException {
        final Method method = type.getDeclaredMethod(methodName, parameterTypes);
        return method.getAnnotation(DistributedLock.class);
    }
}
