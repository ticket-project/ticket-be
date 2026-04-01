package com.ticket.core.infra.redis;

import com.ticket.core.domain.performanceseat.infra.realtime.SeatEventPublisher;
import com.ticket.core.domain.performanceseat.support.SeatRedisKey;
import com.ticket.core.domain.performanceseat.support.SeatStatusMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
class SeatSelectionExpirationHandlerTest {

    @Mock
    private SeatEventPublisher seatEventPublisher;

    @Test
    void 좌석_select_키를_지원하고_deselected_이벤트를_발행한다() {
        SeatSelectionExpirationHandler handler = new SeatSelectionExpirationHandler(seatEventPublisher);
        String expiredKey = SeatRedisKey.select(10L, 20L);

        assertThat(handler.supports(expiredKey)).isTrue();

        handler.handle(expiredKey);

        verify(seatEventPublisher).publish(10L, 20L, SeatStatusMessage.SeatAction.DESELECTED);
    }

    @Test
    void 좌석_select_키가_아니면_지원하지_않는다() {
        SeatSelectionExpirationHandler handler = new SeatSelectionExpirationHandler(seatEventPublisher);

        assertThat(handler.supports("unknown:key")).isFalse();
        verifyNoInteractions(seatEventPublisher);
    }
}
