package com.ticket.core.domain.order.cancel;

import com.ticket.core.domain.hold.application.HoldHistoryRecorder;
import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.model.OrderSeat;
import com.ticket.core.domain.order.shared.OrderTerminationResult;
import com.ticket.core.enums.OrderState;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
class OrderCancelerTest {

    @Mock
    private HoldHistoryRecorder holdHistoryRecorder;

    @InjectMocks
    private OrderCanceler orderCanceler;

    @Test
    void throws_when_order_seat_belongs_to_another_order() {
        final Order order = createOrder(10L, 100L, "hold-key");
        final Order otherOrder = createOrder(11L, 100L, "other-hold-key");
        final OrderSeat foreignOrderSeat = new OrderSeat(otherOrder, 501L, 42L, BigDecimal.TEN);

        assertThatThrownBy(() -> orderCanceler.cancel(order, List.of(foreignOrderSeat), LocalDateTime.of(2026, 3, 15, 10, 0)))
                .isInstanceOf(CoreException.class)
                .satisfies(error -> {
                    assertThat(((CoreException) error).getErrorType()).isEqualTo(ErrorType.INVALID_REQUEST);
                    assertThat(((CoreException) error).getData()).isEqualTo("orderSeats는 같은 order에 속해야 합니다.");
                });
        verifyNoInteractions(holdHistoryRecorder);
    }

    @Test
    void cancels_order_and_records_hold_history() {
        final Order order = createOrder(10L, 100L, "hold-key");
        final OrderSeat orderSeat = new OrderSeat(order, 501L, 42L, BigDecimal.TEN);
        final LocalDateTime now = LocalDateTime.of(2026, 3, 15, 10, 0);

        final OrderTerminationResult result = orderCanceler.cancel(order, List.of(orderSeat), now);

        assertThat(order.getStatus()).isEqualTo(OrderState.CANCELED);
        assertThat(result.performanceId()).isEqualTo(100L);
        assertThat(result.holdKey()).isEqualTo("hold-key");
        assertThat(result.seatIds()).containsExactly(42L);
        verify(holdHistoryRecorder).recordCanceled(1L, 100L, "hold-key", now, List.of(orderSeat));
    }

    private Order createOrder(final Long id, final Long performanceId, final String holdKey) {
        final Order order = new Order(1L, performanceId, "order-key", holdKey, BigDecimal.TEN, LocalDateTime.now().plusMinutes(5));
        ReflectionTestUtils.setField(order, "id", id);
        return order;
    }
}
