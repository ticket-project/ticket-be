package com.ticket.core.domain.show;


import com.ticket.core.domain.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "SHOWS")
public class Show extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    //장소
    private String place;

    protected Show() {}

    public Show(final String title, final String place) {
        this.title = title;
        this.place = place;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getPlace() {
        return place;
    }
}
