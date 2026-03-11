package com.ticket.core.domain.order.model;

import com.ticket.core.domain.BaseEntity;
import com.ticket.core.enums.OrderState;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "ORDERS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long performanceId;

    @Column(nullable = false, unique = true, length = 64)
    private String holdToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OrderState status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime confirmedAt;

    private LocalDateTime expiredAt;

    private LocalDateTime canceledAt;

    private LocalDateTime paymentFailedAt;

    public Order(
            final Long memberId,
            final Long performanceId,
            final String holdToken,
            final BigDecimal totalAmount,
            final LocalDateTime expiresAt
    ) {
        this.memberId = memberId;
        this.performanceId = performanceId;
        this.holdToken = holdToken;
        this.status = OrderState.PENDING;
        this.totalAmount = totalAmount;
        this.expiresAt = expiresAt;
    }

    public void confirm(final LocalDateTime now) {
        this.status = OrderState.CONFIRMED;
        this.confirmedAt = now;
    }

    public void expire(final LocalDateTime now) {
        this.status = OrderState.EXPIRED;
        this.expiredAt = now;
    }

    public void cancel(final LocalDateTime now) {
        this.status = OrderState.CANCELED;
        this.canceledAt = now;
    }

    public void failPayment(final LocalDateTime now) {
        this.status = OrderState.PAYMENT_FAILED;
        this.paymentFailedAt = now;
    }

    public boolean isPending() {
        return status == OrderState.PENDING;
    }

    public boolean isExpired(final LocalDateTime now) {
        return expiresAt.isBefore(now) || expiresAt.isEqual(now);
    }
}
