package com.ticket.core.domain.ticket;

import com.ticket.core.domain.BaseEntity;
import com.ticket.core.domain.order.Order;
import com.ticket.core.domain.performanceseat.PerformanceSeat;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "TICKET")
public class Ticket extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_seat_id", nullable = false)
    private PerformanceSeat performanceSeat;

    private BigDecimal purchasePrice;

    protected Ticket() {}
}
