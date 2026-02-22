package com.ticket.core.domain.member;

import com.ticket.core.domain.BaseEntity;
import com.ticket.core.domain.member.vo.Email;
import com.ticket.core.domain.member.vo.EncodedPassword;
import com.ticket.core.enums.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "MEMBERS", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"email"})
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

    public Member(final Email email, final EncodedPassword encodedPassword, final String name, final Role role) {
        this.email = email;
        this.encodedPassword = encodedPassword;
        this.name = name;
        this.role = role;
    }

    public static Member createSocialMember(
            final Email email,
            final String name,
            final Role role
    ) {
        return new Member(email, null, name, role);
    }

    public void withdraw() {
        markDeleted(LocalDateTime.now());
        this.email = Email.create(buildWithdrawnEmail());
        this.encodedPassword = null;
    }

    private String buildWithdrawnEmail() {
        final String idPart = id == null ? "unknown" : id.toString();
        final String randomPart = UUID.randomUUID().toString().replace("-", "");
        return "deleted_" + idPart + "_" + randomPart + "@withdrawn.ticket";
    }

}
