package com.ticket.core.domain.show;

import com.ticket.core.domain.seat.Seat;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "SHOW_SEATS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShowSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Show show;

    @ManyToOne(fetch = FetchType.LAZY)
    private Seat seat;

    @ManyToOne(fetch = FetchType.LAZY)
    private ShowGrade showGrade;

}
