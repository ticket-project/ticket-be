package com.ticket.core.domain.member;

import com.ticket.core.domain.BaseEntity;
import com.ticket.core.enums.SocialProvider;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Getter
@Entity
@Table(name = "MEMBER_SOCIAL_ACCOUNTS", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"social_provider", "social_id"}),
        @UniqueConstraint(columnNames = {"member_id", "social_provider"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberSocialAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_provider", nullable = false)
    private SocialProvider socialProvider;

    @Column(name = "social_id", nullable = false)
    private String socialId;

    private MemberSocialAccount(
            final Member member,
            final SocialProvider socialProvider,
            final String socialId
    ) {
        this.member = Objects.requireNonNull(member, "member must not be null");
        this.socialProvider = Objects.requireNonNull(socialProvider, "socialProvider must not be null");
        this.socialId = Objects.requireNonNull(socialId, "socialId must not be null");
    }

    public static MemberSocialAccount create(
            final Member member,
            final SocialProvider socialProvider,
            final String socialId
    ) {
        return new MemberSocialAccount(member, socialProvider, socialId);
    }

    public boolean isSameSocialId(final String providerId) {
        return socialId.equals(providerId);
    }
}
