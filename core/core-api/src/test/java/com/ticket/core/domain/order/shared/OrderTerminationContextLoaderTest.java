package com.ticket.core.domain.order.shared;

import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.model.OrderSeat;
import com.ticket.core.domain.order.repository.OrderSeatRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
class OrderTerminationContextLoaderTest {

    @Mock
    private OrderSeatRepository orderSeatRepository;

    @InjectMocks
    private OrderTerminationContextLoader contextLoader;

    @Test
    void 주문_좌석을_조회해_종료_컨텍스트를_만든다() {
        Order order = createOrder(10L, 100L, "hold-key");
        OrderSeat first = new OrderSeat(order, 501L, 42L, BigDecimal.TEN);
        OrderSeat second = new OrderSeat(order, 502L, 43L, BigDecimal.ONE);
        when(orderSeatRepository.findAllByOrder_IdOrderByIdAsc(10L)).thenReturn(List.of(first, second));

        OrderTerminationContext context = contextLoader.load(order);

        assertThat(context.orderSeats()).containsExactly(first, second);
        assertThat(context.seatIds()).containsExactly(42L, 43L);
    }

    private Order createOrder(final Long id, final Long performanceId, final String holdKey) {
        Order order = new Order(1L, performanceId, "order-key", holdKey, BigDecimal.TEN, LocalDateTime.now().plusMinutes(5));
        ReflectionTestUtils.setField(order, "id", id);
        return order;
    }
}
