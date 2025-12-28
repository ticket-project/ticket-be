package com.ticket.storage.db.core;

import com.ticket.core.enums.HoldState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SeatHoldRepository extends JpaRepository<SeatHoldEntity, Long> {
    List<SeatHoldEntity> findAllByExpireAtBeforeAndStateEquals(LocalDateTime expireAtBefore, HoldState state);
}
