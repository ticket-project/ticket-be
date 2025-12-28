package com.ticket.storage.db.core;

import com.ticket.core.enums.HoldState;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "SEAT_HOLD", uniqueConstraints = {
        @UniqueConstraint(
                name = "MEMBER_ID_PERFORMANCE_SEAT_ID",
                columnNames = {"MEMBER_ID", "PERFORMANCE_SEAT_ID"}
        )
})
public class SeatHoldEntity extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    private Long performanceSeatId;

    private LocalDateTime expireAt;

    @Enumerated(EnumType.STRING)
    private HoldState state;

    protected SeatHoldEntity() {}

    public SeatHoldEntity(final Long memberId, final Long performanceSeatId, final LocalDateTime expireAt, final HoldState state) {
        this.memberId = memberId;
        this.performanceSeatId = performanceSeatId;
        this.expireAt = expireAt;
        this.state = state;
    }

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public Long getPerformanceSeatId() {
        return performanceSeatId;
    }

    public LocalDateTime getExpireAt() {
        return expireAt;
    }

    public HoldState getState() {
        return state;
    }

    public void restoreState(final HoldState holdState) {
        this.state = holdState;
    }
}
