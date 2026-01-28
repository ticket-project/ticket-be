package com.ticket.core.domain.show;

import jakarta.persistence.*;

@Entity
@Table(name = "CATEGORYS")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    private Long parentId;

    @Column(nullable = false)
    private String name;

    protected Category() {}
}
