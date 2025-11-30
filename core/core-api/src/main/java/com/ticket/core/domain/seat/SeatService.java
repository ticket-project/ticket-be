package com.ticket.core.domain.seat;

import com.ticket.core.support.exception.ErrorType;
import com.ticket.core.support.exception.NotFoundException;
import com.ticket.storage.db.core.SeatEntity;
import com.ticket.storage.db.core.SeatRepository;
import org.springframework.stereotype.Service;

@Service
public class SeatService {

    private final SeatRepository seatRepository;

    public SeatService(final SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    public Seat findById(final Long id) {
        final SeatEntity seatEntity = seatRepository.findById(id).orElseThrow(() -> new NotFoundException(ErrorType.NOT_FOUND));
        return new Seat(seatEntity.getId(), seatEntity.getPerformanceId(), seatEntity.getX(), seatEntity.getY(), seatEntity.getStatus());
    }
}
