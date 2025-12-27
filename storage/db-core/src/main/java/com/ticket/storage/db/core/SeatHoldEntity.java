package com.ticket.storage.db.core;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "SEAT_HOLD")
public class SeatHoldEntity extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    private Long performanceSeatId;

    private LocalDateTime expireAt;

    protected SeatHoldEntity() {}

    public SeatHoldEntity(final Long memberId, final Long performanceSeatId, final LocalDateTime expireAt) {
        this.memberId = memberId;
        this.performanceSeatId = performanceSeatId;
        this.expireAt = expireAt;
    }
}
