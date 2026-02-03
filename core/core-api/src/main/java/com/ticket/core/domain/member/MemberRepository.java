package com.ticket.core.domain.member;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ticket.core.enums.SocialProvider;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail_Email(String email);

    Optional<Member> findByProviderAndProviderId(SocialProvider provider, String providerId);
}
