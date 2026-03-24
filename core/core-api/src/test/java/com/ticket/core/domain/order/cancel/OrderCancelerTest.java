package com.ticket.core.domain.order.cancel;

import com.ticket.core.domain.hold.application.HoldHistoryRecorder;
import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.model.OrderSeat;
import com.ticket.core.domain.order.shared.OrderTerminationContext;
import com.ticket.core.domain.order.shared.OrderTerminationResult;
import com.ticket.core.enums.OrderSeatState;
import com.ticket.core.enums.OrderState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
class OrderCancelerTest {

    @Mock
    private HoldHistoryRecorder holdHistoryRecorder;

    @InjectMocks
    private OrderCanceler orderCanceler;

    @Test
    void 주문과_주문좌석을_취소하고_hold_history를_기록한다() {
        Order order = createOrder(10L, 100L, "hold-key");
        OrderSeat orderSeat = new OrderSeat(order, 501L, 42L, BigDecimal.TEN);
        OrderTerminationContext context = new OrderTerminationContext(List.of(orderSeat), List.of(42L));
        LocalDateTime now = LocalDateTime.of(2026, 3, 15, 10, 0);

        OrderTerminationResult result = orderCanceler.cancel(order, context, now);

        assertThat(order.getStatus()).isEqualTo(OrderState.CANCELED);
        assertThat(orderSeat.getStatus()).isEqualTo(OrderSeatState.CANCELED);
        assertThat(result.performanceId()).isEqualTo(100L);
        assertThat(result.holdKey()).isEqualTo("hold-key");
        assertThat(result.seatIds()).containsExactly(42L);
        verify(holdHistoryRecorder).recordCanceled(1L, 100L, "hold-key", now, List.of(orderSeat));
    }

    private Order createOrder(final Long id, final Long performanceId, final String holdKey) {
        Order order = new Order(1L, performanceId, "order-key", holdKey, BigDecimal.TEN, LocalDateTime.now().plusMinutes(5));
        ReflectionTestUtils.setField(order, "id", id);
        return order;
    }
}
