package com.ticket.core.domain.performanceseat.query;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticket.core.enums.PerformanceSeatState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.ticket.core.domain.performanceseat.model.QPerformanceSeat.performanceSeat;
import static com.ticket.core.domain.show.mapping.QShowGrade.showGrade;
import static com.ticket.core.domain.show.mapping.QShowSeat.showSeat;

@Repository
@RequiredArgsConstructor
public class SeatAvailabilityQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<SeatAvailabilityCalculator.AvailableSeatRow> findAvailableSeatRows(Long performanceId, Long showId) {
        return queryFactory
                .select(Projections.constructor(SeatAvailabilityCalculator.AvailableSeatRow.class,
                        performanceSeat.seat.id,
                        showGrade.gradeName,
                        showGrade.sortOrder
                ))
                .from(performanceSeat)
                .join(showSeat).on(
                        showSeat.seat.id.eq(performanceSeat.seat.id),
                        showSeat.show.id.eq(showId)
                )
                .join(showGrade).on(showGrade.id.eq(showSeat.showGrade.id))
                .where(
                        performanceSeat.performance.id.eq(performanceId),
                        performanceSeat.state.eq(PerformanceSeatState.AVAILABLE)
                )
                .orderBy(showGrade.sortOrder.asc(), performanceSeat.seat.id.asc())
                .fetch();
    }
}
