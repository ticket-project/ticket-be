package com.ticket.core.domain.show;

import com.ticket.core.domain.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "SHOW_CATEGORYS")
public class ShowCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    protected ShowCategory() {}

}
