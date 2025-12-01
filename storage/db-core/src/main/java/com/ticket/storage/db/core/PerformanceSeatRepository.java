package com.ticket.storage.db.core;

import com.ticket.core.enums.PerformanceSeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PerformanceSeatRepository extends JpaRepository<PerformanceSeatEntity, Long> {
    List<PerformanceSeatEntity> findByPerformanceIdAndSeatIdInAndStatus(Long performanceId, Collection<Long> seatIds, PerformanceSeatStatus status);
}
