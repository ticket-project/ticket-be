package com.ticket.core.domain.show;

import jakarta.persistence.*;

@Entity
@Table(name = "SHOW_CATEGORYS")
public class ShowCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Show show;

    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;

    protected ShowCategory() {}

}
