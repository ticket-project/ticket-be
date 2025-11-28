package com.ticket.storage.db.core;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.util.List;

@Entity
public class ReservationEntity {

    @Id @GeneratedValue
    private Long id;

    private Long memberId;

    private Long performanceId;

    private List<Long> seatIds;

    protected ReservationEntity() {}
}
