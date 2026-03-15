package com.ticket.core.domain.performanceseat.command.usecase;

import com.ticket.core.domain.performanceseat.command.SeatSelectionService;
import com.ticket.core.domain.performanceseat.support.SeatEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
class DeselectSeatUseCaseTest {

    @Mock
    private SeatSelectionService seatSelectionService;
    @Mock
    private SeatEventPublisher seatEventPublisher;

    @InjectMocks
    private DeselectSeatUseCase useCase;

    @Test
    void 좌석_해제시_서비스와_이벤트를_순서대로_호출한다() {
        DeselectSeatUseCase.Input input = new DeselectSeatUseCase.Input(10L, 20L, 1L);

        useCase.execute(input);

        InOrder inOrder = inOrder(seatSelectionService, seatEventPublisher);
        inOrder.verify(seatSelectionService).deselect(10L, 20L, 1L);
        inOrder.verify(seatEventPublisher).publish(org.mockito.ArgumentMatchers.any());
    }
}
