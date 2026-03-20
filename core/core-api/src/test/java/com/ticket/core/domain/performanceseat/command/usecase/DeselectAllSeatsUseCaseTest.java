package com.ticket.core.domain.performanceseat.command.usecase;

import com.ticket.core.domain.member.MemberFinder;
import com.ticket.core.domain.performanceseat.command.DeselectedSeatIds;
import com.ticket.core.domain.performanceseat.command.SeatSelectionService;
import com.ticket.core.domain.performanceseat.support.SeatEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
class DeselectAllSeatsUseCaseTest {

    @Mock
    private MemberFinder memberFinder;
    @Mock
    private SeatSelectionService seatSelectionService;
    @Mock
    private SeatEventPublisher seatEventPublisher;

    @InjectMocks
    private DeselectAllSeatsUseCase useCase;

    @Test
    void 전체_좌석_해제시_모든_좌석에_대한_이벤트를_발행한다() {
        //given
        when(seatSelectionService.deselectAll(10L, 1L)).thenReturn(DeselectedSeatIds.from(List.of(20L, 21L)));

        //when
        useCase.execute(new DeselectAllSeatsUseCase.Input(10L, 1L));

        //then
        verify(memberFinder).findActiveMemberById(1L);
        verify(seatSelectionService).deselectAll(10L, 1L);
        verify(seatEventPublisher, times(2)).publish(org.mockito.ArgumentMatchers.any());
    }
}

