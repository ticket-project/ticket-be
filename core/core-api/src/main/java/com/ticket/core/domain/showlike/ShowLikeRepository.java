package com.ticket.core.domain.showlike;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShowLikeRepository extends JpaRepository<ShowLike, Long> {
    boolean existsByMemberIdAndShow_Id(Long memberId, Long showId);

    Optional<ShowLike> findByMemberIdAndShow_Id(Long memberId, Long showId);

    long countByShow_Id(Long showId);
}
