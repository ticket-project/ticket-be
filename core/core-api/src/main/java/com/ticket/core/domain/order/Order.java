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
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToOne(fetch = LAZY)
    @JoinColumn(unique = true)
    private Hold hold;

    @Enumerated(EnumType.STRING)
    private OrderState state;

    protected Order() {}

    public Order(final Member member, final Hold hold, final OrderState state) {
        this.member = member;
        this.hold = hold;
        this.state = state;
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public Hold getHold() {
        return hold;
    }

    public OrderState getState() {
        return state;
    }
}
