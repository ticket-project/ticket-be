package com.ticket.core.domain.queue.repository;

import com.ticket.core.domain.queue.model.PerformanceQueuePolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PerformanceQueuePolicyRepository extends JpaRepository<PerformanceQueuePolicy, Long> {

    Optional<PerformanceQueuePolicy> findByPerformanceId(Long performanceId);
}
