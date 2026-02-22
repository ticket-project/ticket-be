package com.ticket.core.domain.member;

import com.ticket.core.enums.EntityStatus;
import com.ticket.core.enums.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberSocialAccountRepository extends JpaRepository<MemberSocialAccount, Long> {

    @Query("""
            SELECT msa
            FROM MemberSocialAccount msa
            JOIN FETCH msa.member m
            WHERE msa.socialProvider = :socialProvider
              AND msa.socialId = :socialId
              AND msa.status = :socialAccountStatus
              AND m.status = :memberStatus
            """)
    Optional<MemberSocialAccount> findBySocialProviderAndSocialIdAndStatusAndMember_Status(
            @Param("socialProvider") SocialProvider socialProvider,
            @Param("socialId") String socialId,
            @Param("socialAccountStatus") EntityStatus socialAccountStatus,
            @Param("memberStatus") EntityStatus memberStatus
    );

    Optional<MemberSocialAccount> findByMemberAndSocialProviderAndStatus(
            Member member,
            SocialProvider socialProvider,
            EntityStatus status
    );

    List<MemberSocialAccount> findAllByMember(Member member);
}
