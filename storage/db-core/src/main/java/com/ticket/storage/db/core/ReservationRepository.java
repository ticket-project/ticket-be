package com.ticket.storage.db.core;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {
    List<ReservationEntity> findAllByMemberIdAndPerformanceId(Long memberId, Long performanceId);
}
