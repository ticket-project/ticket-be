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

    private String section;

    private String row_no;

    private String seat_no;

    private boolean isActive;

    public Seat(final String section, final String row_no, final String seat_no, final boolean isActive) {
        this.section = section;
        this.row_no = row_no;
        this.seat_no = seat_no;
        this.isActive = isActive;
    }

}
