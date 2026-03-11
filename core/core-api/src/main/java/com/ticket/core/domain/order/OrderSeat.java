package com.ticket.core.domain.order;

import com.ticket.core.domain.BaseEntity;
import com.ticket.core.enums.OrderSeatState;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private Long performanceSeatId;

    @Column(nullable = false)
    private Long seatId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OrderSeatState status;

    public OrderSeat(
            final Order order,
            final Long performanceSeatId,
            final Long seatId,
            final BigDecimal price
    ) {
        this.order = order;
        this.performanceSeatId = performanceSeatId;
        this.seatId = seatId;
        this.price = price;
        this.status = OrderSeatState.HELD;
    }

    public void expire() {
        this.status = OrderSeatState.EXPIRED;
    }

    public void cancel() {
        this.status = OrderSeatState.CANCELED;
    }
}
