package com.ticket.storage.db.core;

import jakarta.persistence.*;

@Entity
@Table(name = "SHOW")
public class ShowEntity extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
