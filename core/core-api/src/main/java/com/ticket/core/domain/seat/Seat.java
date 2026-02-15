package com.ticket.core.domain.seat;


import com.ticket.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "SEAT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seat extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String section;

    @Column(name = "row_no", nullable = false)
    private String rowNo;

    @Column(name = "seat_no", nullable = false)
    private String seatNo;

    public Seat(final String section, final String rowNo, final String seatNo) {
        this.section = section;
        this.rowNo = rowNo;
        this.seatNo = seatNo;
    }

}
