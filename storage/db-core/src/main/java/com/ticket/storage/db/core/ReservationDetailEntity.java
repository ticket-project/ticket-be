package com.ticket.storage.db.core;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "RESERVATION_DETAIL")
public class ReservationDetailEntity {

    @Id @GeneratedValue
    private Long id;

    private Long reservationId;

    private Long performanceSeatId;

    protected ReservationDetailEntity() {}

    public ReservationDetailEntity(final Long reservationId, final Long performanceSeatId) {
        this.reservationId = reservationId;
        this.performanceSeatId = performanceSeatId;
    }
}
