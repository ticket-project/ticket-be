package com.ticket.core.domain.show;

import jakarta.persistence.*;

@Entity
@Table(name = "CATEGORYS")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long parentId;

    private int depth;

    private String name;

    protected Category() {}
}
