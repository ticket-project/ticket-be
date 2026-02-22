package com.ticket.core.domain.performanceseat;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticket.core.api.controller.response.SeatAvailabilityResponse;
import com.ticket.core.enums.PerformanceSeatState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.ticket.core.domain.performanceseat.QPerformanceSeat.performanceSeat;
import static com.ticket.core.domain.show.QShowGrade.showGrade;
import static com.ticket.core.domain.show.QShowSeat.showSeat;

@Repository
@RequiredArgsConstructor
public class SeatAvailabilityQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 특정 회차의 등급별 잔여 좌석 수를 조회합니다.
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
