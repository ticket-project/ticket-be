package com.ticket.core.domain.performanceseat.query;

import com.ticket.core.domain.show.meta.Region;
import com.ticket.core.domain.support.QueryRepositoryTestSupport;
import com.ticket.core.enums.PerformanceSeatState;
import com.ticket.core.enums.SeatStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Import(SeatMapQueryRepository.class)
@SuppressWarnings("NonAsciiCharacters")
class SeatMapQueryRepositoryTest extends QueryRepositoryTestSupport {

    @Autowired
    private SeatMapQueryRepository seatMapQueryRepository;

    private Long showId;
    private Long performanceId;

    @BeforeEach
    void setUp() throws Exception {
        var venue = persistVenue("공연장", Region.SEOUL);
        var show = persistShow("공연", venue, null, 10L, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(5));
        showId = show.getId();
        var vip = persistShowGrade(show, "VIP", "VIP석", BigDecimal.valueOf(150000), 1);
        var r = persistShowGrade(show, "R", "R석", BigDecimal.valueOf(100000), 2);
        var seat1 = persistSeat("A", "01", "02", 1);
        var seat2 = persistSeat("A", "01", "01", 1);
        persistShowSeat(show, seat1, r);
        persistShowSeat(show, seat2, vip);

        var performance = persistPerformance(show, 1L, LocalDateTime.now().plusDays(1));
        performanceId = performance.getId();
        persistPerformanceSeat(performance, seat1, PerformanceSeatState.RESERVED, BigDecimal.valueOf(100000));
        persistPerformanceSeat(performance, seat2, PerformanceSeatState.AVAILABLE, BigDecimal.valueOf(150000));
        flushAndClear();
    }

    @Test
    void 공연_좌석정보를_정렬해서_조회한다() {
        var result = seatMapQueryRepository.findShowSeats(showId);

        assertThat(result).extracting("seatId").hasSize(2);
        assertThat(result).extracting("gradeCode").containsExactly("VIP", "R");
        assertThat(result).extracting("col").containsExactly("01", "02");
    }

    @Test
    void 회차별_좌석상태를_API_상태로_변환한다() {
        var result = seatMapQueryRepository.findSeatStatuses(performanceId);

        assertThat(result).containsExactly(
                new com.ticket.core.api.controller.response.SeatStatusResponse.SeatState(result.get(0).seatId(), SeatStatus.OCCUPIED),
                new com.ticket.core.api.controller.response.SeatStatusResponse.SeatState(result.get(1).seatId(), SeatStatus.AVAILABLE)
        );
    }
}
