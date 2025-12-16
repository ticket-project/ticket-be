package com.ticket.storage.db.core;

import com.ticket.core.enums.EntityStatus;
import com.ticket.core.enums.PerformanceState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PerformanceRepository extends JpaRepository<PerformanceEntity, Long> {
    Optional<PerformanceEntity> findByIdAndStateAndStatus(Long performanceId, PerformanceState state, EntityStatus status);
}
