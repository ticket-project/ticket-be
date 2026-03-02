package com.ticket.core.domain.show;

import com.ticket.core.enums.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShowJpaRepository extends JpaRepository<Show, Long> {
    Optional<Show> findByIdAndStatus(Long id, EntityStatus status);
}
