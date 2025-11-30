package com.ticket.storage.db.core;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<SeatEntity, Long> {
    Long countByPerformanceIdAndStatus(final Long performanceId, final SeatStatus status);
}
