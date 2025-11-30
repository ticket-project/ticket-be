package com.ticket.core.domain.performance;

import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import com.ticket.core.support.exception.NotFoundException;
import com.ticket.storage.db.core.PerformanceEntity;
import com.ticket.storage.db.core.PerformanceRepository;
import com.ticket.storage.db.core.SeatRepository;
import com.ticket.storage.db.core.SeatStatus;
import org.springframework.stereotype.Service;

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

    public void findAllSeatByPerformance(final Long performanceId) {
        final Long availableCount = seatRepository.countByPerformanceIdAndStatus(performanceId, SeatStatus.AVAILABLE);
        if (availableCount <= 0) throw new CoreException(ErrorType.NOT_EXIST_AVAILABLE_SEAT);

    }
}
