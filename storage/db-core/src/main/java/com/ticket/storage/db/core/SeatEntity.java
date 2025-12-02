package com.ticket.storage.db.core;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "SEAT")
public class SeatEntity {

    @Id
    @GeneratedValue
    private Long id;

    //행
    private String x;

    //열
    private String y;

    protected SeatEntity() {}

    public SeatEntity(final Long id, final String x, final String y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public Long getId() {
        return id;
    }

    public String getX() {
        return x;
    }

    public String getY() {
        return y;
    }

}
