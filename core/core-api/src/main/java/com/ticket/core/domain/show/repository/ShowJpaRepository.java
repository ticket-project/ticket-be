package com.ticket.core.domain.show.repository;

import com.ticket.core.domain.show.Show;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShowJpaRepository extends JpaRepository<Show, Long> {
}
