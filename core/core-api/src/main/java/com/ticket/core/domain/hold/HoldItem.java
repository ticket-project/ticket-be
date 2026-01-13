package com.ticket.core.domain.hold;

import com.ticket.core.domain.BaseEntity;
import com.ticket.core.domain.performanceseat.PerformanceSeat;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "HOLD_ITEM")
public class HoldItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Hold hold;

    @ManyToOne(fetch = FetchType.LAZY)
    private PerformanceSeat performanceSeat;

    private BigDecimal holdPrice; //필요할까?

    protected HoldItem() {}

    public HoldItem(final Hold hold, final PerformanceSeat performanceSeat, final BigDecimal holdPrice) {
        this.hold = hold;
        this.performanceSeat = performanceSeat;
        this.holdPrice = holdPrice;
    }
}
