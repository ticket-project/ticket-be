package com.ticket.core.domain.performanceseat.query;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticket.core.api.controller.response.SeatStatusResponse;
import com.ticket.core.api.controller.response.ShowSeatResponse;
import com.ticket.core.enums.PerformanceSeatState;
import com.ticket.core.enums.SeatStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.ticket.core.domain.performanceseat.model.QPerformanceSeat.performanceSeat;
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
     * 회차별 좌석 상태를 조회합니다.
     * DB의 PerformanceSeatState를 API 응답용 SeatStatus로 변환합니다.
     * AVAILABLE → AVAILABLE, RESERVED → OCCUPIED
     */
    public List<SeatStatusResponse.SeatState> findSeatStatuses(Long performanceId) {
        return queryFactory
                .select(performanceSeat.seat.id, performanceSeat.state)
                .from(performanceSeat)
                .where(performanceSeat.performance.id.eq(performanceId))
                .orderBy(performanceSeat.seat.id.asc())
                .fetch()
                .stream()
                .map(tuple -> new SeatStatusResponse.SeatState(
                        tuple.get(performanceSeat.seat.id),
                        tuple.get(performanceSeat.state) == PerformanceSeatState.AVAILABLE
                                ? SeatStatus.AVAILABLE
                                : SeatStatus.OCCUPIED
                ))
                .toList();
    }

}
