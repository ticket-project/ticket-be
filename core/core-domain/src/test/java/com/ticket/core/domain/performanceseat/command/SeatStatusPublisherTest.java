package com.ticket.core.domain.performanceseat.command;

import com.ticket.core.domain.performanceseat.support.SeatEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.ticket.core.domain.performanceseat.support.SeatStatusMessage.SeatAction.HELD;
import static com.ticket.core.domain.performanceseat.support.SeatStatusMessage.SeatAction.RELEASED;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class SeatStatusPublisherTest {

    @Mock
    private SeatEventPublisher seatEventPublisher;

    @InjectMocks
    private SeatStatusPublisher seatStatusPublisher;

    @Test
    void 좌석_선점상태는_좌석별_HELD_이벤트로_발행한다() {
        seatStatusPublisher.publishHeld(10L, List.of(1L, 2L));

        verify(seatEventPublisher).publish(10L, 1L, HELD);
        verify(seatEventPublisher).publish(10L, 2L, HELD);
        verify(seatEventPublisher, times(2)).publish(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void 좌석_해제상태는_좌석별_RELEASED_이벤트로_발행한다() {
        seatStatusPublisher.publishReleased(10L, List.of(3L, 4L));

        verify(seatEventPublisher).publish(10L, 3L, RELEASED);
        verify(seatEventPublisher).publish(10L, 4L, RELEASED);
        verify(seatEventPublisher, times(2)).publish(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.any());
    }
}
