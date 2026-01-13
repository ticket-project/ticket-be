package com.ticket.core.domain.ticket;

import com.ticket.core.domain.order.Order;
import com.ticket.core.domain.performanceseat.PerformanceSeat;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "TICKET")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    private PerformanceSeat performanceSeat;

    private BigDecimal purchasePrice;

    protected Ticket() {}
}
