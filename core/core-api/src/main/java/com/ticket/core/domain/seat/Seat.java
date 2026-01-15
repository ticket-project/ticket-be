package com.ticket.core.domain.seat;


import com.ticket.core.domain.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "SEAT")
public class Seat extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //행
    private String seatRow;

    //열
    private String seatCol;

    protected Seat() {}

    public Seat(final String seatRow, final String seatCol) {
        this.seatRow = seatRow;
        this.seatCol = seatCol;
    }

    public Long getId() {
        return id;
    }

    public String getSeatRow() {
        return seatRow;
    }

    public String getSeatCol() {
        return seatCol;
    }

}
