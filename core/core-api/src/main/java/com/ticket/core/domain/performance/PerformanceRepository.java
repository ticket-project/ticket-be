package com.ticket.core.domain.performance;

import com.ticket.core.enums.EntityStatus;
import com.ticket.core.enums.PerformanceState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PerformanceRepository extends JpaRepository<Performance, Long> {
    Optional<Performance> findByIdAndStateAndStatus(Long performanceId, PerformanceState state, EntityStatus status);
}
