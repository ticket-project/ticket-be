package com.ticket.member;

import com.ticket.member.vo.Email;
import jakarta.persistence.*;

@Entity
@Table(name = "MEMBERS")
public class Member {

    @Id
    @GeneratedValue
    private Long id;

    @Embedded
    private Email email;
    private String name;
    private String password;

    protected Member() {
    }

    public static Member register(final Email email, final String password, final String name) {
        return new Member(email, password, name);
    }

    private Member(final Email email, final String password, final String name) {
        this.email = email;
        this.password = password;
        this.name = name;
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

    public String getPassword() {
        return password;
    }
}
