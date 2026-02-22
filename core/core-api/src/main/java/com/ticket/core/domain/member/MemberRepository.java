package com.ticket.core.domain.member;

import com.ticket.core.enums.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail_EmailAndStatus(String email, EntityStatus status);

    Optional<Member> findByIdAndStatus(Long id, EntityStatus status);
}
