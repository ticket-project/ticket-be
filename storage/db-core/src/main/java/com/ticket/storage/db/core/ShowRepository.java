package com.ticket.storage.db.core;

import com.ticket.core.enums.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShowRepository extends JpaRepository<ShowEntity, Long> {
    Optional<ShowEntity> findByIdAndStatus(Long showId, EntityStatus status);
}
