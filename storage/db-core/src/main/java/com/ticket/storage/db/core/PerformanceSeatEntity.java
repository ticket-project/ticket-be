package com.ticket.storage.db.core;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class PerformanceSeatEntity {

    @Id @GeneratedValue
    private Long id;

    private Long performanceId;

    private Long seatId;

    protected PerformanceSeatEntity() {}
}
