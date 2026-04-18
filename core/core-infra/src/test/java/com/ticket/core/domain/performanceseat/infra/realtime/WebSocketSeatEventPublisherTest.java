package com.ticket.core.domain.performanceseat.infra.realtime;

import com.ticket.core.domain.performanceseat.support.SeatStatusMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
class WebSocketSeatEventPublisherTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-03-15T01:00:00Z"), ZoneId.of("Asia/Seoul"));

    @Test
    void 좌석_이벤트_발행은_Clock_기준_시간을_메시지에_포함한다() {
        WebSocketSeatEventPublisher publisher = new WebSocketSeatEventPublisher(messagingTemplate, fixedClock);
        ArgumentCaptor<SeatStatusMessage> captor = ArgumentCaptor.forClass(SeatStatusMessage.class);

        publisher.publish(10L, 20L, SeatStatusMessage.SeatAction.HELD);

        verify(messagingTemplate).convertAndSend(eq("/topic/performance/10/seats"), captor.capture());
        assertThat(captor.getValue().timestamp()).isEqualTo(LocalDateTime.of(2026, 3, 15, 10, 0));
        assertThat(captor.getValue().action()).isEqualTo(SeatStatusMessage.SeatAction.HELD);
    }
}
