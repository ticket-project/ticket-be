package com.ticket.storage.db.core;

import jakarta.persistence.*;

@Entity
@Table(name = "MEMBERS")
public class MemberEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String email;
    private String name;
    private String password;

    protected MemberEntity() {}

    public MemberEntity(final String email, final String password, final String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }
}
