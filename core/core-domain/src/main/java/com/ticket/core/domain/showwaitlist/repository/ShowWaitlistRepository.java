package com.ticket.core.domain.showwaitlist.repository;

import com.ticket.core.domain.showwaitlist.model.ShowWaitlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShowWaitlistRepository extends JpaRepository<ShowWaitlist, Long> {
    boolean existsByMember_IdAndShow_Id(Long memberId, Long showId);

    Optional<ShowWaitlist> findByMember_IdAndShow_Id(Long memberId, Long showId);

    long countByShow_Id(Long showId);
}
