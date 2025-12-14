package com.ticket.storage.db.core;

import jakarta.persistence.*;

@Entity
@Table(name = "RESERVATION")
public class ReservationEntity extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    private Long performanceId;

    protected ReservationEntity() {}

    public ReservationEntity(final Long memberId, final Long performanceId) {
        this.memberId = memberId;
        this.performanceId = performanceId;
    }

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public Long getPerformanceId() {
        return performanceId;
    }

}
