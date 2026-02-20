package com.ticket.core.domain.member;

import com.ticket.core.enums.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberSocialAccountRepository extends JpaRepository<MemberSocialAccount, Long> {

    @Query("SELECT msa FROM MemberSocialAccount msa JOIN FETCH msa.member WHERE msa.socialProvider = :socialProvider AND msa.socialId = :socialId")
    Optional<MemberSocialAccount> findBySocialProviderAndSocialId(@Param("socialProvider") SocialProvider socialProvider, @Param("socialId") String socialId);

    Optional<MemberSocialAccount> findByMemberAndSocialProvider(Member member, SocialProvider socialProvider);
}
