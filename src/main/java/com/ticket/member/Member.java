package com.ticket.member;

import com.ticket.member.vo.Email;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Email email;
    private String name;
    private String password;

    public static Member register(final String email, final String password, final String name) {
        return new Member(new Email(email), password, name);
    }

    private Member(final Email email, final String password, final String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }
}
