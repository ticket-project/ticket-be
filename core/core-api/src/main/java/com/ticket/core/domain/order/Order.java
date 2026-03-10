package com.ticket.core.domain.order;

import com.ticket.core.domain.BaseEntity;
import com.ticket.core.enums.OrderState;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

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

    @Column(nullable = false, unique = true, length = 30)
    private String orderNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderState state;

    @Column(nullable = false, precision = 10, scale = 0)
    private BigDecimal totalAmount;

    private LocalDateTime confirmedAt;

    private LocalDateTime cancelledAt;

    private Order(final Long memberId, final Long performanceId, final String orderNo, final BigDecimal totalAmount) {
        this.memberId = memberId;
        this.performanceId = performanceId;
        this.orderNo = orderNo;
        this.state = OrderState.PENDING;
        this.totalAmount = totalAmount;
    }

    public static Order create(final Long memberId, final Long performanceId, final BigDecimal totalAmount) {
        final String orderNo = generateOrderNo();
        return new Order(memberId, performanceId, orderNo, totalAmount);
    }

    public void confirm() {
        validateState(OrderState.PENDING, "주문 확정");
        this.state = OrderState.COMPLETED;
        this.confirmedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (this.state == OrderState.CANCELLED) {
            return; // 이미 취소됨 — 멱등 처리
        }
        this.state = OrderState.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    public boolean isPending() {
        return this.state == OrderState.PENDING;
    }

    public boolean isOwnedBy(final Long memberId) {
        return this.memberId.equals(memberId);
    }

    private void validateState(final OrderState expected, final String action) {
        if (this.state != expected) {
            throw new IllegalStateException(
                    String.format("%s은(는) %s 상태에서만 가능합니다. 현재 상태: %s", action, expected, this.state)
            );
        }
    }

    private static String generateOrderNo() {
        final String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        final String uuidPart = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "ORD-" + datePart + "-" + uuidPart;
    }
}

