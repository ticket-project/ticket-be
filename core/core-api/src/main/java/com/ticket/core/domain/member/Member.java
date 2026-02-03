package com.ticket.core.domain.member;

import com.ticket.core.domain.BaseEntity;
import com.ticket.core.domain.member.vo.Email;
import com.ticket.core.domain.member.vo.EncodedPassword;
import com.ticket.core.enums.Role;
import com.ticket.core.enums.SocialProvider;
import jakarta.persistence.*;

@Entity
@Table(name = "MEMBERS", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"email"}),
        @UniqueConstraint(columnNames = {"provider", "providerId"})
})
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
    private SocialProvider provider;

    private String providerId;

    private String profileImageUrl;

    protected Member() {}

    public Member(final Email email, final EncodedPassword encodedPassword, final String name, final Role role) {
        this(email, encodedPassword, name, role, null, null, null);
    }

    public Member(
            final Email email,
            final EncodedPassword encodedPassword,
            final String name,
            final Role role,
            final SocialProvider provider,
            final String providerId,
            final String profileImageUrl
    ) {
        this.email = email;
        this.encodedPassword = encodedPassword;
        this.name = name;
        this.role = role;
        this.provider = provider;
        this.providerId = providerId;
        this.profileImageUrl = profileImageUrl;
    }

    public Long getId() {
        return id;
    }

    public Email getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public EncodedPassword getEncodedPassword() {
        return encodedPassword;
    }

    public Role getRole() {
        return role;
    }

    public SocialProvider getProvider() {
        return provider;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void updateSocialInfo(final SocialProvider provider, final String providerId, final String profileImageUrl, final String name) {
        this.provider = provider;
        this.providerId = providerId;
        if (profileImageUrl != null && !profileImageUrl.isBlank()) {
            this.profileImageUrl = profileImageUrl;
        }
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
    }
}
