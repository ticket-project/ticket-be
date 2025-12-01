package com.ticket.core.domain.performance;

import com.ticket.core.domain.seat.Seat;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import com.ticket.core.support.exception.NotFoundException;
import com.ticket.storage.db.core.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PerformanceService {

    private final PerformanceRepository performanceRepository;
    private final SeatRepository seatRepository;

    public PerformanceService(final PerformanceRepository performanceRepository, final SeatRepository seatRepository) {
        this.performanceRepository = performanceRepository;
        this.seatRepository = seatRepository;
    }

    public Performance findById(final Long id) {
        final PerformanceEntity performanceEntity = performanceRepository.findById(id).orElseThrow(() -> new NotFoundException(ErrorType.NOT_FOUND));
        return new Performance(performanceEntity.getId(), performanceEntity.getStartTime(), performanceEntity.getEndTime(), performanceEntity.getReserveOpenTime(), performanceEntity.getReserveCloseTime());
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
