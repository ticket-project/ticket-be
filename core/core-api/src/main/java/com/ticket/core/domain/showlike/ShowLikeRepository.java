package com.ticket.core.domain.showlike;

import com.ticket.core.enums.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShowLikeRepository extends JpaRepository<ShowLike, Long> {
    boolean existsByMember_IdAndShow_Id(Long memberId, Long showId);

    Optional<ShowLike> findByMember_IdAndShow_Id(Long memberId, Long showId);

    long countByShow_IdAndStatus(Long showId, EntityStatus status);
}
