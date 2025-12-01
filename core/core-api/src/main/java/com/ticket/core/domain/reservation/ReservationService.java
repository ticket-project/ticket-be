package com.ticket.core.domain.reservation;

import com.ticket.storage.db.core.ReservationRepository;
import com.ticket.storage.db.core.SeatRepository;
import org.springframework.stereotype.Service;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;

    public ReservationService(final ReservationRepository reservationRepository,
                              final SeatRepository seatRepository) {
        this.reservationRepository = reservationRepository;
        this.seatRepository = seatRepository;
    }

}
