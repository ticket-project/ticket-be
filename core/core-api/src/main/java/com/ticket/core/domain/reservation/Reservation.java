package com.ticket.core.domain.reservation;

import com.ticket.core.domain.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "RESERVATIONS")
public class Reservation extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    private Long performanceId;

    protected Reservation() {}

    public Reservation(final Long memberId, final Long performanceId) {
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
