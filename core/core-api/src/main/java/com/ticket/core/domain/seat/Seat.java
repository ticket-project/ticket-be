package com.ticket.core.domain.seat;

public class Seat {

    private final Long id;

    //행
    private final String x;

    //열
    private final String y;

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
