package com.ticket.core.domain.performanceseat;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticket.core.api.controller.response.SeatAvailabilityResponse;
import com.ticket.core.enums.PerformanceSeatState;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.ticket.core.domain.performanceseat.QPerformanceSeat.performanceSeat;
import static com.ticket.core.domain.show.QShowGrade.showGrade;
import static com.ticket.core.domain.show.QShowSeat.showSeat;

@Repository
public class SeatAvailabilityQueryRepository {

    private final JPAQueryFactory queryFactory;

    public SeatAvailabilityQueryRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 특정 회차의 등급별 잔여석 현황을 조회합니다.
     *
     * PerformanceSeat → ShowSeat (seat_id 기준) → ShowGrade
     * ShowGrade 기준 GROUP BY하여 등급별 총석수 / 잔여석수 집계
     */
    public SeatAvailabilityResponse findSeatAvailability(Long performanceId, Long showId) {

        List<SeatAvailabilityResponse.GradeAvailability> grades = queryFactory
                .select(Projections.constructor(SeatAvailabilityResponse.GradeAvailability.class,
                        showGrade.gradeName,
                        showGrade.sortOrder,
                        performanceSeat.state
                                .when(PerformanceSeatState.AVAILABLE).then(1L)
                                .otherwise(0L)
                                .sum()
                ))
                .from(performanceSeat)
                .join(showSeat).on(
                        showSeat.seat.id.eq(performanceSeat.seat.id),
                        showSeat.show.id.eq(showId)
                )
                .join(showGrade).on(showGrade.id.eq(showSeat.showGrade.id))
                .where(performanceSeat.performance.id.eq(performanceId))
                .groupBy(showGrade.id, showGrade.gradeCode, showGrade.gradeName, showGrade.price, showGrade.sortOrder)
                .orderBy(showGrade.sortOrder.asc())
                .fetch();

        return new SeatAvailabilityResponse(grades);
    }
}
