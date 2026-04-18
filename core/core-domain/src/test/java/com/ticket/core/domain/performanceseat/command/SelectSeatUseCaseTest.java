package com.ticket.core.domain.performanceseat.command;

import com.ticket.core.domain.performanceseat.support.SeatSelectionAvailabilityValidator;
import com.ticket.core.domain.performanceseat.support.SeatStatusMessage.SeatAction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
class SelectSeatUseCaseTest {

    @Mock
    private SeatSelectionService seatSelectionService;

    @Mock
    private SeatSelectionAvailabilityValidator seatSelectionAvailabilityValidator;

    @Mock
    private SeatEventPort seatEventPort;

    @InjectMocks
    private SelectSeatUseCase useCase;

    @Test
    void select_then_publish_selected_event() {
        SelectSeatUseCase.Input input = new SelectSeatUseCase.Input(10L, 20L, 1L);

        useCase.execute(input);

        InOrder inOrder = inOrder(seatSelectionAvailabilityValidator, seatSelectionService, seatEventPort);
        inOrder.verify(seatSelectionAvailabilityValidator).validate(10L, 20L);
        inOrder.verify(seatSelectionService).select(10L, 20L, 1L);
        inOrder.verify(seatEventPort).publish(10L, 20L, SeatAction.SELECTED);
    }
}
