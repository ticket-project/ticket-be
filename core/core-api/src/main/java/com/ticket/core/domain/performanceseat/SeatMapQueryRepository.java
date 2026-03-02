package com.ticket.core.domain.performanceseat;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticket.core.api.controller.response.SeatStatusResponse;
import com.ticket.core.api.controller.response.ShowSeatResponse;
import com.ticket.core.api.controller.response.VenueLayoutResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.ticket.core.domain.performanceseat.QPerformanceSeat.performanceSeat;
import static com.ticket.core.domain.seat.QSeat.seat;
import static com.ticket.core.domain.show.QShowGrade.showGrade;
import static com.ticket.core.domain.show.QShowSeat.showSeat;

@Repository
@RequiredArgsConstructor
public class SeatMapQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 공연의 좌석 정보를 조회합니다 (좌표 + 등급, 상태 제외).
     * ShowSeat ↔ Seat ↔ ShowGrade JOIN
     */
    public List<ShowSeatResponse.SeatInfo> findShowSeats(Long showId) {
        return queryFactory
                .select(Projections.constructor(ShowSeatResponse.SeatInfo.class,
                        seat.id,
                        seat.floor,
                        seat.section,
                        seat.rowNo,
                        seat.seatNo,
                        seat.x,
                        seat.y,
                        showGrade.gradeCode,
                        showGrade.gradeName,
                        showGrade.price
                ))
                .from(showSeat)
                .join(seat).on(seat.id.eq(showSeat.seat.id))
                .join(showGrade).on(showGrade.id.eq(showSeat.showGrade.id))
                .where(showSeat.show.id.eq(showId))
                .orderBy(seat.floor.asc(), seat.section.asc(), seat.rowNo.asc(), seat.seatNo.asc())
                .fetch();
    }

    /**
     * 회차별 좌석 상태를 조회합니다 (seatId + state만).
     */
    public List<SeatStatusResponse.SeatState> findSeatStatuses(Long performanceId) {
        return queryFactory
                .select(Projections.constructor(SeatStatusResponse.SeatState.class,
                        performanceSeat.seat.id,
                        performanceSeat.state.stringValue()
                ))
                .from(performanceSeat)
                .where(performanceSeat.performance.id.eq(performanceId))
                .orderBy(performanceSeat.seat.id.asc())
                .fetch();
    }

}
