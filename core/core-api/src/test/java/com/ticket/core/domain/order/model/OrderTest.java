package com.ticket.core.domain.order.model;

import com.ticket.core.enums.OrderState;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("NonAsciiCharacters")
class OrderTest {

    @Test
    void 주문을_생성하면_pending_상태로_초기화된다() {
        LocalDateTime expiresAt = LocalDateTime.of(2026, 3, 15, 12, 30);

        Order order = createOrder(expiresAt);

        assertThat(order.getMemberId()).isEqualTo(1L);
        assertThat(order.getPerformanceId()).isEqualTo(10L);
        assertThat(order.getOrderKey()).isEqualTo("order-key");
        assertThat(order.getHoldKey()).isEqualTo("hold-key");
        assertThat(order.getStatus()).isEqualTo(OrderState.PENDING);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("15000");
        assertThat(order.getExpiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void pending_주문은_확정할_수_있다() {
        LocalDateTime now = LocalDateTime.of(2026, 3, 15, 12, 0);
        Order order = createOrder(now.plusMinutes(10));

        order.confirm(now);

        assertThat(order.getStatus()).isEqualTo(OrderState.CONFIRMED);
        assertThat(order.getConfirmedAt()).isEqualTo(now);
    }

    @Test
    void pending_주문은_만료할_수_있다() {
        LocalDateTime now = LocalDateTime.of(2026, 3, 15, 12, 0);
        Order order = createOrder(now.plusMinutes(10));

        order.expire(now);

        assertThat(order.getStatus()).isEqualTo(OrderState.EXPIRED);
        assertThat(order.getExpiredAt()).isEqualTo(now);
    }

    @Test
    void pending_주문은_취소할_수_있다() {
        LocalDateTime now = LocalDateTime.of(2026, 3, 15, 12, 0);
        Order order = createOrder(now.plusMinutes(10));

        order.cancel(now);

        assertThat(order.getStatus()).isEqualTo(OrderState.CANCELED);
        assertThat(order.getCanceledAt()).isEqualTo(now);
    }

    @Test
    void pending_주문은_결제실패로_전이할_수_있다() {
        LocalDateTime now = LocalDateTime.of(2026, 3, 15, 12, 0);
        Order order = createOrder(now.plusMinutes(10));

        order.failPayment(now);

        assertThat(order.getStatus()).isEqualTo(OrderState.PAYMENT_FAILED);
        assertThat(order.getPaymentFailedAt()).isEqualTo(now);
    }

    @Test
    void pending이_아닌_주문은_다시_전이할_수_없다() {
        Order order = createOrder(LocalDateTime.of(2026, 3, 15, 12, 30));
        order.confirm(LocalDateTime.of(2026, 3, 15, 12, 0));

        assertThatThrownBy(() -> order.cancel(LocalDateTime.of(2026, 3, 15, 12, 5)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("currentStatus=CONFIRMED");
    }

    @Test
    void 만료시각과_같거나_지난_pending_주문은_만료된_것으로_본다() {
        LocalDateTime expiresAt = LocalDateTime.of(2026, 3, 15, 12, 30);
        Order order = createOrder(expiresAt);

        assertThat(order.isExpired(expiresAt.minusSeconds(1))).isFalse();
        assertThat(order.isExpired(expiresAt)).isTrue();
        assertThat(order.isExpired(expiresAt.plusSeconds(1))).isTrue();
    }

    @Test
    void pending이_아닌_주문은_만료시각이_지나도_isExpired가_false다() {
        LocalDateTime expiresAt = LocalDateTime.of(2026, 3, 15, 12, 30);
        Order order = createOrder(expiresAt);
        order.confirm(expiresAt.minusMinutes(1));

        assertThat(order.isExpired(expiresAt.plusMinutes(1))).isFalse();
    }

    private Order createOrder(final LocalDateTime expiresAt) {
        return new Order(1L, 10L, "order-key", "hold-key", BigDecimal.valueOf(15000), expiresAt);
    }
}
