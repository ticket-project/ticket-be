package com.ticket.core.domain.member;

import com.ticket.core.enums.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberSocialAccountRepository extends JpaRepository<MemberSocialAccount, Long> {

    Optional<MemberSocialAccount> findBySocialProviderAndSocialId(SocialProvider socialProvider, String socialId);

    Optional<MemberSocialAccount> findByMemberAndSocialProvider(Member member, SocialProvider socialProvider);
}
