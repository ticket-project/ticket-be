package com.ticket.core.domain.order;

import com.ticket.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "ORDER_SEATS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderSeat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "performance_seat_id", nullable = false)
    private Long performanceSeatId;

    @Column(nullable = false, precision = 10, scale = 0)
    private BigDecimal price;

    private OrderSeat(final Order order, final Long performanceSeatId, final BigDecimal price) {
        this.order = order;
        this.performanceSeatId = performanceSeatId;
        this.price = price;
    }

    public static OrderSeat create(final Order order, final Long performanceSeatId, final BigDecimal price) {
        return new OrderSeat(order, performanceSeatId, price);
    }
}
