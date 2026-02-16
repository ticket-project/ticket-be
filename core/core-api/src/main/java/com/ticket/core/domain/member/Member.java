package com.ticket.core.domain.member;

import com.ticket.core.domain.BaseEntity;
import com.ticket.core.domain.member.vo.Email;
import com.ticket.core.domain.member.vo.EncodedPassword;
import com.ticket.core.enums.Role;
import com.ticket.core.enums.SocialProvider;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "MEMBERS", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"email"}),
        @UniqueConstraint(columnNames = {"social_provider", "social_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Email email;

    @Embedded
    private EncodedPassword encodedPassword;

    private String name;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_provider")
    private SocialProvider socialProvider;

    @Column(name = "social_id")
    private String socialId;

    public Member(final Email email, final EncodedPassword encodedPassword, final String name, final Role role) {
        this(email, encodedPassword, name, role, null, null);
    }

    public Member(
            final Email email,
            final EncodedPassword encodedPassword,
            final String name,
            final Role role,
            final SocialProvider socialProvider,
            final String socialId
    ) {
        this.email = email;
        this.encodedPassword = encodedPassword;
        this.name = name;
        this.role = role;
        this.socialProvider = socialProvider;
        this.socialId = socialId;
    }

    public static Member createSocialMember(
            final Email email,
            final String name,
            final Role role,
            final SocialProvider socialProvider,
            final String socialId
    ) {
        return new Member(email, null, name, role, socialProvider, socialId);
    }

    public void linkSocialAccount(final SocialProvider socialProvider, final String socialId) {
        this.socialProvider = socialProvider;
        this.socialId = socialId;
    }

}
