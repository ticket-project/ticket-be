package com.ticket.core.domain.performanceseat.query;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticket.core.domain.performanceseat.query.model.SeatStatus;
import com.ticket.core.domain.performanceseat.model.PerformanceSeatState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.ticket.core.domain.performanceseat.model.QPerformanceSeat.performanceSeat;
import static com.ticket.core.domain.seat.model.QSeat.seat;
import static com.ticket.core.domain.show.mapping.QShowGrade.showGrade;
import static com.ticket.core.domain.show.mapping.QShowSeat.showSeat;

@Repository
@RequiredArgsConstructor
public class SeatMapQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<GetShowSeatsUseCase.SeatInfo> findShowSeats(final Long showId) {
        return queryFactory
                .select(Projections.constructor(GetShowSeatsUseCase.SeatInfo.class,
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

    public List<GetSeatStatusUseCase.SeatState> findSeatStatuses(final Long performanceId) {
        return queryFactory
                .select(performanceSeat.seat.id, performanceSeat.state)
                .from(performanceSeat)
                .where(performanceSeat.performance.id.eq(performanceId))
                .orderBy(performanceSeat.seat.id.asc())
                .fetch()
                .stream()
                .map(tuple -> new GetSeatStatusUseCase.SeatState(
                        tuple.get(performanceSeat.seat.id),
                        tuple.get(performanceSeat.state) == PerformanceSeatState.AVAILABLE
                                ? SeatStatus.AVAILABLE
                                : SeatStatus.OCCUPIED
                ))
                .toList();
    }
}
