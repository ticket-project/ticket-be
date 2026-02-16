package com.ticket.core.domain.member;

import com.ticket.core.enums.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail_Email(String email);

    Optional<Member> findBySocialProviderAndSocialId(SocialProvider socialProvider, String socialId);
}
