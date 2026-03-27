package com.ticket.core.infra.redis;

import com.ticket.core.domain.order.command.expire.ExpireOrderUseCase;
import com.ticket.core.domain.performanceseat.infra.realtime.SeatEventPublisher;
import com.ticket.core.domain.performanceseat.support.SeatRedisKey;
import com.ticket.core.domain.performanceseat.support.SeatStatusMessage;
import com.ticket.core.domain.queue.command.QueueAdmissionAdvancer;
import com.ticket.core.domain.queue.model.QueueEntryId;
import com.ticket.core.domain.queue.runtime.QueueRedisKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.Message;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
class RedisKeyExpirationListenerTest {

    @Mock
    private SeatEventPublisher seatEventPublisher;

    @Mock
    private ExpireOrderUseCase expireOrderUseCase;

    @Mock
    private QueueAdmissionAdvancer queueAdmissionAdvancer;

    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-03-15T01:00:00Z"), ZoneId.of("Asia/Seoul"));

    @Test
    void 홀드_만료_이벤트는_Clock_기준_시간으로_주문_만료를_처리한다() {
        RedisKeyExpirationListener listener = new RedisKeyExpirationListener(
                seatEventPublisher,
                expireOrderUseCase,
                queueAdmissionAdvancer,
                fixedClock
        );

        listener.onMessage(message(SeatRedisKey.holdMeta("hold-key")), new byte[0]);

        verify(expireOrderUseCase).expireByHoldKey("hold-key", LocalDateTime.of(2026, 3, 15, 10, 0));
        verifyNoInteractions(queueAdmissionAdvancer);
    }

    @Test
    void 좌석_선택_만료_이벤트는_DESELECTED를_발행한다() {
        RedisKeyExpirationListener listener = new RedisKeyExpirationListener(
                seatEventPublisher,
                expireOrderUseCase,
                queueAdmissionAdvancer,
                fixedClock
        );

        listener.onMessage(message(SeatRedisKey.select(10L, 20L)), new byte[0]);

        verify(seatEventPublisher).publish(10L, 20L, SeatStatusMessage.SeatAction.DESELECTED);
        verifyNoInteractions(expireOrderUseCase, queueAdmissionAdvancer);
    }

    @Test
    void 대기열_토큰_만료_이벤트는_입장_만료를_처리한다() {
        RedisKeyExpirationListener listener = new RedisKeyExpirationListener(
                seatEventPublisher,
                expireOrderUseCase,
                queueAdmissionAdvancer,
                fixedClock
        );
        String queueToken = QueueRedisKey.createToken(30L, "entry-1", "token-1");

        listener.onMessage(message(QueueRedisKey.tokenStorageKey(queueToken)), new byte[0]);

        verify(queueAdmissionAdvancer).handleTokenExpired(30L, QueueEntryId.from("entry-1"), queueToken);
        verifyNoInteractions(seatEventPublisher, expireOrderUseCase);
    }

    private Message message(final String body) {
        return new Message() {
            @Override
            public byte[] getBody() {
                return body.getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public byte[] getChannel() {
                return null;
            }
        };
    }
}
