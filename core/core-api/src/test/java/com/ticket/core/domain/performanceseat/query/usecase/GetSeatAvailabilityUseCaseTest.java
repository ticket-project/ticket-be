package com.ticket.core.domain.performanceseat.query.usecase;

import com.ticket.core.api.controller.response.SeatAvailabilityResponse;
import com.ticket.core.domain.hold.support.HoldManager;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceFinder;
import com.ticket.core.domain.performanceseat.command.SeatSelectionService;
import com.ticket.core.domain.performanceseat.query.SeatAvailabilityCalculator;
import com.ticket.core.domain.performanceseat.query.SeatAvailabilityQueryRepository;
import com.ticket.core.domain.show.Show;
import com.ticket.core.enums.PerformanceSeatState;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
class GetSeatAvailabilityUseCaseTest {

    @Mock
    private PerformanceFinder performanceFinder;
    @Mock
    private SeatAvailabilityQueryRepository seatAvailabilityQueryRepository;
    @Mock
    private HoldManager holdManager;
    @Mock
    private SeatSelectionService seatSelectionService;
    @Mock
    private SeatAvailabilityCalculator seatAvailabilityCalculator;

    @InjectMocks
    private GetSeatAvailabilityUseCase useCase;

    @Test
    void DB와_redis_점유좌석을_합쳐_잔여석을_계산한다() {
        //given
        Performance performance = mock(Performance.class);
        Show show = mock(Show.class);
        List<SeatAvailabilityCalculator.AvailableSeatRow> rows =
                List.of(new SeatAvailabilityCalculator.AvailableSeatRow(1L, PerformanceSeatState.AVAILABLE, "VIP", 1));
        SeatAvailabilityResponse response = new SeatAvailabilityResponse(List.of(new SeatAvailabilityResponse.GradeAvailability("VIP", 1, 0L)));

        when(performanceFinder.findById(10L)).thenReturn(performance);
        when(performance.getId()).thenReturn(10L);
        when(performance.getShow()).thenReturn(show);
        when(show.getId()).thenReturn(100L);
        when(seatAvailabilityQueryRepository.findAvailableSeatRows(10L, 100L)).thenReturn(rows);
        when(seatSelectionService.getSelectingSeatIds(10L)).thenReturn(Set.of(1L));
        when(holdManager.getHoldingSeatIds(10L)).thenReturn(Set.of(2L));
        when(seatAvailabilityCalculator.calculate(rows, Set.of(1L, 2L))).thenReturn(response);

        //when
        GetSeatAvailabilityUseCase.Output output = useCase.execute(new GetSeatAvailabilityUseCase.Input(10L));

        //then
        assertThat(output.availability()).isEqualTo(response);
        verify(seatAvailabilityCalculator).calculate(rows, Set.of(1L, 2L));
    }

    @Test
    void 공연이_연결되지_않은_회차면_예외를_던진다() {
        //given
        Performance performance = mock(Performance.class);
        when(performanceFinder.findById(10L)).thenReturn(performance);
        when(performance.getShow()).thenReturn(null);

        //when
        //then
        assertThatThrownBy(() -> useCase.execute(new GetSeatAvailabilityUseCase.Input(10L)))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> assertThat(((CoreException) exception).getErrorType()).isEqualTo(ErrorType.NOT_FOUND_DATA));
    }
}

