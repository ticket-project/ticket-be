package com.ticket.core.domain.hold.repository;

import com.ticket.core.domain.hold.model.HoldHistory;
import com.ticket.core.enums.HoldState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HoldHistoryRepository extends JpaRepository<HoldHistory, Long> {

    List<HoldHistory> findAllByHoldKeyOrderByIdAsc(String holdKey);

    List<HoldHistory> findAllByHoldKeyAndStatusOrderByIdAsc(String holdKey, HoldState status);
}
