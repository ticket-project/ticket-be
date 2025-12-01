package com.ticket.core.domain.performanceseat;

import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.reservation.AddReservation;
import com.ticket.core.domain.seat.Seat;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import com.ticket.core.support.exception.NotFoundException;
import com.ticket.storage.db.core.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PerformanceSeatService {

    private final PerformanceRepository performanceRepository;
    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;

    public PerformanceSeatService(final PerformanceRepository performanceRepository, final SeatRepository seatRepository, final ReservationRepository reservationRepository) {
        this.performanceRepository = performanceRepository;
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
    }

    public Long reserve(final Member member, final NewPerformanceSeats newPerformanceSeats) {
        final List<Long> seatIds = newPerformanceSeats.getSeatIds();
        final Map<Long, SeatEntity> seatMap = seatRepository.findByIdIn(seatIds).stream()
                .collect(Collectors.toMap(SeatEntity::getId, p -> p));
        if (seatMap.isEmpty()) throw new CoreException(ErrorType.NOT_FOUND);
        if (!seatMap.keySet().equals(seatIds)) {
            throw new CoreException(ErrorType.SEAT_MISMATCH_IN_PERFORMANCE);
        }

        final ReservationEntity reservationEntity = new ReservationEntity(member.getId(), newPerformanceSeats.getPerformanceId(), foundSeatIds);
        final ReservationEntity savedReservation = reservationRepository.save(reservationEntity);
        return savedReservation.getId();
    }

    public List<Seat> findAllSeatByPerformance(final Long performanceId) {
        final Long availableCount = seatRepository.countByPerformanceIdAndStatus(performanceId, SeatStatus.AVAILABLE);
        if (availableCount <= 0) throw new CoreException(ErrorType.NOT_EXIST_AVAILABLE_SEAT);
        final List<SeatEntity> seatEntities = seatRepository.findByPerformanceId(performanceId);
        if (seatEntities.isEmpty()) {
            throw new NotFoundException(ErrorType.NOT_FOUND);
        }
        return seatEntities.stream()
                .map(e -> new Seat(e.getId(),
                        e.getPerformanceId(),
                        e.getX(),
                        e.getY(),
                        e.getStatus())
                )
                .toList();
    }
}
