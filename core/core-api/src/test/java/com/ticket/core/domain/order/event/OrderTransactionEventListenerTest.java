package com.ticket.core.domain.order.event;

import com.ticket.core.domain.hold.support.HoldManager;
import com.ticket.core.domain.performanceseat.application.SeatStatusPublishApplicationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class OrderTransactionEventListenerTest {

    @Mock
    private HoldManager holdManager;

    @Mock
    private SeatStatusPublishApplicationService seatStatusPublishApplicationService;

    @InjectMocks
    private OrderTransactionEventListener listener;

    @Test
    void 주문취소_이벤트를_받으면_hold를_해제하고_좌석상태를_발행한다() {
        OrderCancelledEvent event = new OrderCancelledEvent(1L, "hold-key", List.of(10L, 20L));

        listener.handle(event);

        verify(holdManager).release(1L, "hold-key", List.of(10L, 20L));
        verify(seatStatusPublishApplicationService).publishReleased(1L, List.of(10L, 20L));
    }

    @Test
    void 주문만료_이벤트를_받으면_hold를_해제하고_좌석상태를_발행한다() {
        OrderExpiredEvent event = new OrderExpiredEvent(1L, "hold-key", List.of(10L, 20L));

        listener.handle(event);

        verify(holdManager).release(1L, "hold-key", List.of(10L, 20L));
        verify(seatStatusPublishApplicationService).publishReleased(1L, List.of(10L, 20L));
    }
}
