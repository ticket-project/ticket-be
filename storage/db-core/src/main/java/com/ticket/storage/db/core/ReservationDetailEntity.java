package com.ticket.storage.db.core;

import jakarta.persistence.*;

@Entity
@Table(name = "RESERVATION_DETAIL")
public class ReservationDetailEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long reservationId;

    private Long performanceSeatId;

    protected ReservationDetailEntity() {}

    public ReservationDetailEntity(final Long reservationId, final Long performanceSeatId) {
        this.reservationId = reservationId;
        this.performanceSeatId = performanceSeatId;
    }
}
