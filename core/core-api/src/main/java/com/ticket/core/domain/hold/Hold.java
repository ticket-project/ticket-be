package com.ticket.core.domain.hold;

import com.ticket.core.domain.BaseEntity;
import com.ticket.core.domain.member.Member;
import com.ticket.core.enums.HoldState;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "HOLDS")
public class Hold extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private LocalDateTime expireAt;

    @Enumerated(EnumType.STRING)
    private HoldState state;

    protected Hold() {}

    public Hold(final Member member, final LocalDateTime expireAt, final HoldState state) {
        this.member = member;
        this.expireAt = expireAt;
        this.state = state;
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public LocalDateTime getExpireAt() {
        return expireAt;
    }

    public HoldState getState() {
        return state;
    }
}
