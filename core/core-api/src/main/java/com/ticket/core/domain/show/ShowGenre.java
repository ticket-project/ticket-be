package com.ticket.core.domain.show;

import com.ticket.core.domain.BaseEntity;
import jakarta.persistence.*;

/**
 * 공연-장르 연결 엔티티
 * - 공연은 0개 이상의 장르를 가질 수 있음
 */
@Entity
@Table(name = "SHOW_GENRES")
public class ShowGenre extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "genre_id", nullable = false)
    private Genre genre;

    protected ShowGenre() {}

    public ShowGenre(Show show, Genre genre) {
        this.show = show;
        this.genre = genre;
    }

    public Long getId() {
        return id;
    }

    public Show getShow() {
        return show;
    }

    public Genre getGenre() {
        return genre;
    }
}
