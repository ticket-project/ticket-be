package com.ticket.core.domain.order.domainservice;

import com.ticket.core.domain.hold.model.HoldHistory;
import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.model.OrderSeat;
import com.ticket.core.enums.HoldReleaseReason;
import com.ticket.core.enums.HoldState;
import com.ticket.core.enums.OrderSeatState;
import com.ticket.core.enums.OrderState;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
class OrderLifecycleDomainServiceTest {

    private final OrderLifecycleDomainService orderLifecycleDomainService = new OrderLifecycleDomainService();

    @Test
    void 취소시_주문과_좌석과_홀드이력을_함께_취소한다() {
        LocalDateTime now = LocalDateTime.of(2026, 3, 15, 12, 0);
        Order order = new Order(1L, 10L, "order-key", "hold-key", BigDecimal.TEN, now.plusMinutes(5));
        OrderSeat orderSeat = new OrderSeat(order, 100L, 200L, BigDecimal.TEN);
        HoldHistory holdHistory = new HoldHistory("hold-key", 1L, 10L, 100L, 200L, now.plusMinutes(5));

        orderLifecycleDomainService.cancel(order, List.of(orderSeat), List.of(holdHistory), now);

        assertThat(order.getStatus()).isEqualTo(OrderState.CANCELED);
        assertThat(orderSeat.getStatus()).isEqualTo(OrderSeatState.CANCELED);
        assertThat(holdHistory.getStatus()).isEqualTo(HoldState.CANCELED);
        assertThat(holdHistory.getReleaseReason()).isEqualTo(HoldReleaseReason.USER_CANCELED);
        assertThat(holdHistory.getReleasedAt()).isEqualTo(now);
    }

    @Test
    void 만료시_주문과_좌석과_홀드이력을_함께_만료한다() {
        LocalDateTime now = LocalDateTime.of(2026, 3, 15, 12, 0);
        Order order = new Order(1L, 10L, "order-key", "hold-key", BigDecimal.TEN, now.plusMinutes(5));
        OrderSeat orderSeat = new OrderSeat(order, 100L, 200L, BigDecimal.TEN);
        HoldHistory holdHistory = new HoldHistory("hold-key", 1L, 10L, 100L, 200L, now.plusMinutes(5));

        orderLifecycleDomainService.expire(order, List.of(orderSeat), List.of(holdHistory), now);

        assertThat(order.getStatus()).isEqualTo(OrderState.EXPIRED);
        assertThat(orderSeat.getStatus()).isEqualTo(OrderSeatState.EXPIRED);
        assertThat(holdHistory.getStatus()).isEqualTo(HoldState.EXPIRED);
        assertThat(holdHistory.getReleaseReason()).isEqualTo(HoldReleaseReason.TTL_EXPIRED);
        assertThat(holdHistory.getReleasedAt()).isEqualTo(now);
    }
}
