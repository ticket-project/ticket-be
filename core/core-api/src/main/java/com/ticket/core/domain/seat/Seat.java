package com.ticket.core.domain.seat;


import jakarta.persistence.*;

@Entity
@Table(name = "SEAT")
public class Seat {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //행
    private String x;

    //열
    private String y;

    protected Seat() {}

    public Seat(final Long id, final String x, final String y) {
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
