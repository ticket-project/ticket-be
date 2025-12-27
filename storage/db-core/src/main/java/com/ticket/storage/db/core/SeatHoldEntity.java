package com.ticket.storage.db.core;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "SEAT_HOLD")
public class SeatHoldEntity extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long performanceSeatId;

    private Long memberId;

    private LocalDateTime expireAt;
}
