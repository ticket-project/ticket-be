package com.ticket.storage.db.core;

import com.ticket.core.enums.PerformanceSeatState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PerformanceSeatRepository extends JpaRepository<PerformanceSeatEntity, Long> {
    List<PerformanceSeatEntity> findByPerformanceIdAndSeatIdInAndState(Long performanceId, List<Long> seatIds, PerformanceSeatState state);
}
