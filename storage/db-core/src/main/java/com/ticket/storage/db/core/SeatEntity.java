package com.ticket.storage.db.core;


import jakarta.persistence.*;

@Entity
@Table(name = "SEAT")
public class SeatEntity extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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
