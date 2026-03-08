package com.ticket.core.domain.performance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PerformanceRepository extends JpaRepository<Performance, Long> {
    List<Performance> findAllByShowIdOrderByStartTimeAscPerformanceNoAsc(Long showId);
}