package com.ticket.core.domain.hold.repository;

import com.ticket.core.domain.hold.model.HoldHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HoldHistoryRepository extends JpaRepository<HoldHistory, Long> {

    List<HoldHistory> findAllByHoldTokenOrderByIdAsc(String holdToken);
}
