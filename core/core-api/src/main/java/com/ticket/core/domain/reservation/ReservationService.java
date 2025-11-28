package com.ticket.core.domain.reservation;

import com.ticket.storage.db.core.ReservationRepository;
import org.springframework.stereotype.Service;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService(final ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public void reserve(final AddReservation addReservation) {

    }
}
