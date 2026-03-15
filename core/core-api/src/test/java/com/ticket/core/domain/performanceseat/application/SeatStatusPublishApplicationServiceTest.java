package com.ticket.core.domain.performanceseat.application;

import com.ticket.core.domain.performanceseat.support.SeatEventPublisher;
import com.ticket.core.domain.performanceseat.support.SeatStatusMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.ticket.core.domain.performanceseat.support.SeatStatusMessage.SeatAction.HELD;
import static com.ticket.core.domain.performanceseat.support.SeatStatusMessage.SeatAction.RELEASED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class SeatStatusPublishApplicationServiceTest {

    @Mock
    private SeatEventPublisher seatEventPublisher;

    @InjectMocks
    private SeatStatusPublishApplicationService seatStatusPublishApplicationService;

    @Test
    void 좌석_선점상태는_좌석별_HELD_이벤트로_발행한다() {
        //given
        seatStatusPublishApplicationService.publishHeld(10L, List.of(1L, 2L));

        //when
        ArgumentCaptor<SeatStatusMessage> captor = ArgumentCaptor.forClass(SeatStatusMessage.class);
        //then
        verify(seatEventPublisher, times(2)).publish(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(SeatStatusMessage::performanceId, SeatStatusMessage::seatId, SeatStatusMessage::action)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(10L, 1L, HELD),
                        org.assertj.core.groups.Tuple.tuple(10L, 2L, HELD)
                );
    }

    @Test
    void 좌석_해제상태는_좌석별_RELEASED_이벤트로_발행한다() {
        //given
        seatStatusPublishApplicationService.publishReleased(10L, List.of(3L, 4L));

        //when
        ArgumentCaptor<SeatStatusMessage> captor = ArgumentCaptor.forClass(SeatStatusMessage.class);
        //then
        verify(seatEventPublisher, times(2)).publish(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(SeatStatusMessage::performanceId, SeatStatusMessage::seatId, SeatStatusMessage::action)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(10L, 3L, RELEASED),
                        org.assertj.core.groups.Tuple.tuple(10L, 4L, RELEASED)
                );
    }
}

