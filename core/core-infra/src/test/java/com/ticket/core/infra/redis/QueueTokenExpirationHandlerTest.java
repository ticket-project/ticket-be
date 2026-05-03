package com.ticket.core.infra.redis;

import com.ticket.core.domain.queue.command.QueueAdmissionAdvancer;
import com.ticket.core.domain.queue.model.QueueEntryId;
import com.ticket.core.domain.queue.runtime.QueueRedisKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
class QueueTokenExpirationHandlerTest {

    @Mock
    private QueueAdmissionAdvancer queueAdmissionAdvancer;

    @Test
    void queue_token_키를_지원하고_대기열_만료를_위임한다() {
        QueueTokenExpirationHandler handler = new QueueTokenExpirationHandler(queueAdmissionAdvancer);
        String queueToken = QueueRedisKey.createToken(30L, "entry-1", "token-1");
        String expiredKey = QueueRedisKey.tokenStorageKey(queueToken);

        assertThat(handler.supports(expiredKey)).isTrue();

        handler.handle(expiredKey);

        verify(queueAdmissionAdvancer).handleTokenExpired(30L, QueueEntryId.from("entry-1"), queueToken);
    }

    @Test
    void queue_token_키가_아니면_지원하지_않는다() {
        QueueTokenExpirationHandler handler = new QueueTokenExpirationHandler(queueAdmissionAdvancer);

        assertThat(handler.supports("unknown:key")).isFalse();
        verifyNoInteractions(queueAdmissionAdvancer);
    }
}
