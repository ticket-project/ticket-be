package com.ticket.core.domain.order.model;

import com.ticket.core.enums.OrderSeatState;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("NonAsciiCharacters")
class OrderSeatTest {

    @Test
    void 주문좌석을_생성하면_held_상태로_초기화된다() {
        //given
        Order order = createOrder();

        //when
        OrderSeat orderSeat = new OrderSeat(order, 100L, 200L, BigDecimal.valueOf(12000));

        //then
        assertThat(orderSeat.getOrder()).isSameAs(order);
        assertThat(orderSeat.getPerformanceSeatId()).isEqualTo(100L);
        assertThat(orderSeat.getSeatId()).isEqualTo(200L);
        assertThat(orderSeat.getPrice()).isEqualByComparingTo("12000");
        assertThat(orderSeat.getStatus()).isEqualTo(OrderSeatState.HELD);
    }

    @Test
    void held_주문좌석은_확정할_수_있다() {
        //given
        OrderSeat orderSeat = createOrderSeat();

        //when
        orderSeat.confirm();

        //then
        assertThat(orderSeat.getStatus()).isEqualTo(OrderSeatState.CONFIRMED);
    }

    @Test
    void held_주문좌석은_만료할_수_있다() {
        //given
        OrderSeat orderSeat = createOrderSeat();

        //when
        orderSeat.expire();

        //then
        assertThat(orderSeat.getStatus()).isEqualTo(OrderSeatState.EXPIRED);
    }

    @Test
    void held_주문좌석은_취소할_수_있다() {
        //given
        OrderSeat orderSeat = createOrderSeat();

        //when
        orderSeat.cancel();

        //then
        assertThat(orderSeat.getStatus()).isEqualTo(OrderSeatState.CANCELED);
    }

    @Test
    void held가_아닌_주문좌석은_다시_전이할_수_없다() {
        //given
        OrderSeat orderSeat = createOrderSeat();
        orderSeat.confirm();

        //when
        //then
        assertThatThrownBy(orderSeat::cancel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("currentStatus=CONFIRMED");
    }

    private OrderSeat createOrderSeat() {
        return new OrderSeat(createOrder(), 100L, 200L, BigDecimal.valueOf(12000));
    }

    private Order createOrder() {
        return new Order(
                1L,
                10L,
                "order-key",
                "hold-key",
                BigDecimal.valueOf(12000),
                LocalDateTime.of(2026, 3, 15, 12, 30)
        );
    }
}

