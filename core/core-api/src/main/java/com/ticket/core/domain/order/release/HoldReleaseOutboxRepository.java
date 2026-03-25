package com.ticket.core.domain.order.release;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface HoldReleaseOutboxRepository extends JpaRepository<HoldReleaseOutbox, Long> {

    Slice<HoldReleaseOutbox> findAllByCompletedAtIsNullAndNextAttemptAtLessThanEqual(
            LocalDateTime nextAttemptAt,
            Pageable pageable
    );
}
