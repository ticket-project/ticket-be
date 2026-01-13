package com.ticket.core.domain.seat;


import com.ticket.core.domain.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "SEAT")
public class Seat extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //행
    private String row;

    //열
    private String column;

    protected Seat() {}

    public Seat(final String row, final String column) {
        this.row = row;
        this.column = column;
    }

    public Long getId() {
        return id;
    }

    public String getRow() {
        return row;
    }

    public String getColumn() {
        return column;
    }

}
