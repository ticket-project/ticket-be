package com.ticket.core.domain.reservation;

import jakarta.persistence.*;

@Entity
@Table(name = "RESERVATION_DETAILS")
public class ReservationDetail {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long reservationId;

    private Long performanceSeatId;

    protected ReservationDetail() {}

    public ReservationDetail(final Long reservationId, final Long performanceSeatId) {
        this.reservationId = reservationId;
        this.performanceSeatId = performanceSeatId;
    }
}
