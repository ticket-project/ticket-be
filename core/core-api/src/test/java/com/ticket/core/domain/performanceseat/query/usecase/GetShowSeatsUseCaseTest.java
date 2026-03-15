package com.ticket.core.domain.performanceseat.query.usecase;

import com.ticket.core.api.controller.response.ShowSeatResponse;
import com.ticket.core.domain.performanceseat.query.SeatMapQueryRepository;
import com.ticket.core.domain.show.Show;
import com.ticket.core.domain.show.query.ShowFinder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetShowSeatsUseCaseTest {

    @Mock
    private ShowFinder showFinder;
    @Mock
    private SeatMapQueryRepository seatMapQueryRepository;

    @InjectMocks
    private GetShowSeatsUseCase useCase;

    @Test
    void 공연_좌석정보를_조회한다() {
        Show show = mock(Show.class);
        List<ShowSeatResponse.SeatInfo> seats = List.of(
                new ShowSeatResponse.SeatInfo(1L, 1, "A", "10", "7", 10.0, 20.0, "VIP", "VIP", BigDecimal.TEN)
        );
        when(showFinder.findById(100L)).thenReturn(show);
        when(show.getId()).thenReturn(100L);
        when(seatMapQueryRepository.findShowSeats(100L)).thenReturn(seats);

        GetShowSeatsUseCase.Output output = useCase.execute(new GetShowSeatsUseCase.Input(100L));

        assertThat(output.seatInfo().seats()).isEqualTo(seats);
        verify(seatMapQueryRepository).findShowSeats(100L);
    }
}
