package com.ticket.core.domain.order.expire;

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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
class OrderExpirerTest {

    @Mock
    private HoldHistoryRecorder holdHistoryRecorder;

    @InjectMocks
    private OrderExpirer orderExpirer;

    @Test
    void pending이_아닌_주문이면_빈_결과를_반환한다() {
        Order order = createOrder(10L, 100L, "hold-key");
        order.cancel(LocalDateTime.of(2026, 3, 15, 9, 0));
        OrderTerminationContext context = new OrderTerminationContext(List.of(), List.of());

        Optional<OrderTerminationResult> result = orderExpirer.expire(order, context, LocalDateTime.of(2026, 3, 15, 10, 0));

        assertThat(result).isEmpty();
        verifyNoInteractions(holdHistoryRecorder);
    }

    @Test
    void pending_주문이면_주문과_주문좌석을_만료하고_hold_history를_기록한다() {
        Order order = createOrder(10L, 100L, "hold-key");
        OrderSeat orderSeat = new OrderSeat(order, 501L, 42L, BigDecimal.TEN);
        OrderTerminationContext context = new OrderTerminationContext(List.of(orderSeat), List.of(42L));
        LocalDateTime now = LocalDateTime.of(2026, 3, 15, 10, 0);

        Optional<OrderTerminationResult> result = orderExpirer.expire(order, context, now);

        assertThat(result).isPresent();
        assertThat(order.getStatus()).isEqualTo(OrderState.EXPIRED);
        assertThat(orderSeat.getStatus()).isEqualTo(OrderSeatState.EXPIRED);
        assertThat(result.get().performanceId()).isEqualTo(100L);
        assertThat(result.get().holdKey()).isEqualTo("hold-key");
        assertThat(result.get().seatIds()).containsExactly(42L);
        verify(holdHistoryRecorder).recordExpired(1L, 100L, "hold-key", now, List.of(orderSeat));
    }

    private Order createOrder(final Long id, final Long performanceId, final String holdKey) {
        Order order = new Order(1L, performanceId, "order-key", holdKey, BigDecimal.TEN, LocalDateTime.now().plusMinutes(5));
        ReflectionTestUtils.setField(order, "id", id);
        return order;
    }
}
