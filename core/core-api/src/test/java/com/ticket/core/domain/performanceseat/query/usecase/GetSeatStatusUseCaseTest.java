package com.ticket.core.domain.performanceseat.query.usecase;

import com.ticket.core.api.controller.response.SeatStatusResponse;
import com.ticket.core.domain.hold.support.HoldManager;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceFinder;
import com.ticket.core.domain.performanceseat.command.SeatSelectionService;
import com.ticket.core.domain.performanceseat.query.SeatMapQueryRepository;
import com.ticket.core.domain.performanceseat.query.model.SeatStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
class GetSeatStatusUseCaseTest {

    @Mock
    private PerformanceFinder performanceFinder;
    @Mock
    private SeatMapQueryRepository seatMapQueryRepository;
    @Mock
    private SeatSelectionService seatSelectionService;
    @Mock
    private HoldManager holdManager;

    @InjectMocks
    private GetSeatStatusUseCase useCase;

    @Test
    void redis에서_점유중인_available_좌석은_occupied로_변환한다() {
        //given
        Performance performance = mock(Performance.class);
        when(performanceFinder.findById(10L)).thenReturn(performance);
        when(performance.getId()).thenReturn(10L);
        when(seatMapQueryRepository.findSeatStatuses(10L)).thenReturn(List.of(
                new SeatStatusResponse.SeatState(1L, SeatStatus.AVAILABLE),
                new SeatStatusResponse.SeatState(2L, SeatStatus.OCCUPIED)
        ));
        when(seatSelectionService.getSelectingSeatIds(10L)).thenReturn(Set.of(1L));
        when(holdManager.getHoldingSeatIds(10L)).thenReturn(Set.of());

        //when
        GetSeatStatusUseCase.Output output = useCase.execute(new GetSeatStatusUseCase.Input(10L));

        //then
        assertThat(output.seats()).containsExactly(
                new SeatStatusResponse.SeatState(1L, SeatStatus.OCCUPIED),
                new SeatStatusResponse.SeatState(2L, SeatStatus.OCCUPIED)
        );
    }

    @Test
    void redis_점유좌석이_없으면_DB상태를_그대로_반환한다() {
        //given
        Performance performance = mock(Performance.class);
        List<SeatStatusResponse.SeatState> dbStates = List.of(
                new SeatStatusResponse.SeatState(1L, SeatStatus.AVAILABLE),
                new SeatStatusResponse.SeatState(2L, SeatStatus.OCCUPIED)
        );
        when(performanceFinder.findById(10L)).thenReturn(performance);
        when(performance.getId()).thenReturn(10L);
        when(seatMapQueryRepository.findSeatStatuses(10L)).thenReturn(dbStates);
        when(seatSelectionService.getSelectingSeatIds(10L)).thenReturn(Set.of());
        when(holdManager.getHoldingSeatIds(10L)).thenReturn(Set.of());

        //when
        GetSeatStatusUseCase.Output output = useCase.execute(new GetSeatStatusUseCase.Input(10L));

        //then
        assertThat(output.seats()).containsExactlyElementsOf(dbStates);
        verify(seatMapQueryRepository).findSeatStatuses(10L);
    }
}
