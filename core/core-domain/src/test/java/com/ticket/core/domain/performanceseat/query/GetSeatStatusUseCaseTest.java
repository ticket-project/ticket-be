package com.ticket.core.domain.performanceseat.query;

import com.ticket.core.domain.hold.command.HoldManager;
import com.ticket.core.domain.performance.model.Performance;
import com.ticket.core.domain.performance.query.PerformanceFinder;
import com.ticket.core.domain.performanceseat.command.SeatSelectionService;
import com.ticket.core.domain.performanceseat.query.model.SeatStateView;
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
    void redis가_점유중인_available_좌석은_occupied로_변환한다() {
        Performance performance = mock(Performance.class);
        when(performanceFinder.findById(10L)).thenReturn(performance);
        when(performance.getId()).thenReturn(10L);
        when(seatMapQueryRepository.findSeatStatuses(10L)).thenReturn(List.of(
                new SeatStateView(1L, SeatStatus.AVAILABLE),
                new SeatStateView(2L, SeatStatus.OCCUPIED)
        ));
        when(seatSelectionService.getSelectingSeatIds(10L)).thenReturn(Set.of(1L));
        when(holdManager.getHoldingSeatIds(10L)).thenReturn(Set.of());

        GetSeatStatusUseCase.Output output = useCase.execute(new GetSeatStatusUseCase.Input(10L));

        assertThat(output.seats()).containsExactly(
                new SeatStateView(1L, SeatStatus.OCCUPIED),
                new SeatStateView(2L, SeatStatus.OCCUPIED)
        );
    }

    @Test
    void redis_점유좌석이_없으면_db_상태를_그대로_반환한다() {
        Performance performance = mock(Performance.class);
        List<SeatStateView> dbStates = List.of(
                new SeatStateView(1L, SeatStatus.AVAILABLE),
                new SeatStateView(2L, SeatStatus.OCCUPIED)
        );
        when(performanceFinder.findById(10L)).thenReturn(performance);
        when(performance.getId()).thenReturn(10L);
        when(seatMapQueryRepository.findSeatStatuses(10L)).thenReturn(dbStates);
        when(seatSelectionService.getSelectingSeatIds(10L)).thenReturn(Set.of());
        when(holdManager.getHoldingSeatIds(10L)).thenReturn(Set.of());

        GetSeatStatusUseCase.Output output = useCase.execute(new GetSeatStatusUseCase.Input(10L));

        assertThat(output.seats()).containsExactlyElementsOf(dbStates);
        verify(seatMapQueryRepository).findSeatStatuses(10L);
    }
}
