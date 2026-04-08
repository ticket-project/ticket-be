package com.ticket.core.domain.performeralert.repository;

import com.ticket.core.domain.performeralert.model.PerformerAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PerformerAlertRepository extends JpaRepository<PerformerAlert, Long> {
    boolean existsByMember_IdAndPerformer_Id(Long memberId, Long performerId);

    Optional<PerformerAlert> findByMember_IdAndPerformer_Id(Long memberId, Long performerId);

    long countByPerformer_Id(Long performerId);
}
