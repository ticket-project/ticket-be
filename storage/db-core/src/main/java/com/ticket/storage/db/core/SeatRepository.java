package com.ticket.storage.db.core;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<SeatEntity, Long> {
    Long countByPerformanceIdAndStatus(final Long performanceId, final SeatStatus status);

    Optional<List<SeatEntity>> findByPerformanceId(Long performanceId);
}
