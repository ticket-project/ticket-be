package com.ticket.core.domain.order;

import com.ticket.core.domain.BaseEntity;
import com.ticket.core.domain.hold.Hold;
import com.ticket.core.domain.member.Member;
import com.ticket.core.enums.OrderState;
import jakarta.persistence.*;

import static jakarta.persistence.FetchType.*;

@Entity
@Table(name = "ORDERS")
public class Order extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    private Member member;

    @OneToOne(fetch = LAZY)
    private Hold hold;

    @Enumerated(EnumType.STRING)
    private OrderState state;
}
