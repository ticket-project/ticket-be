package com.ticket.core.domain.performanceseat.query;

import com.ticket.core.domain.show.meta.Region;
import com.ticket.core.domain.support.QueryRepositoryTestSupport;
import com.ticket.core.enums.PerformanceSeatState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Import(SeatAvailabilityQueryRepository.class)
class SeatAvailabilityQueryRepositoryTest extends QueryRepositoryTestSupport {

    @Autowired
    private SeatAvailabilityQueryRepository seatAvailabilityQueryRepository;

    private Long showId;
    private Long performanceId;

    @BeforeEach
    void setUp() throws Exception {
        var venue = persistVenue("공연장", Region.SEOUL);
        var show = persistShow("공연", venue, null, 10L, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(5));
        showId = show.getId();
        var vip = persistShowGrade(show, "VIP", "VIP석", BigDecimal.valueOf(150000), 1);
        var r = persistShowGrade(show, "R", "R석", BigDecimal.valueOf(100000), 2);
        var seat1 = persistSeat("A", "01", "01", 1);
        var seat2 = persistSeat("A", "01", "02", 1);
        persistShowSeat(show, seat1, vip);
        persistShowSeat(show, seat2, r);

        var performance = persistPerformance(show, 1L, LocalDateTime.now().plusDays(1));
        performanceId = performance.getId();
        persistPerformanceSeat(performance, seat2, PerformanceSeatState.RESERVED, BigDecimal.valueOf(100000));
        persistPerformanceSeat(performance, seat1, PerformanceSeatState.AVAILABLE, BigDecimal.valueOf(150000));
        flushAndClear();
    }

    @Test
    void 등급정렬과_좌석ID순으로_가용좌석_원본행을_조회한다() {
        var result = seatAvailabilityQueryRepository.findAvailableSeatRows(performanceId, showId);

        assertThat(result).extracting("gradeName").containsExactly("VIP석", "R석");
        assertThat(result).extracting("state").containsExactly(PerformanceSeatState.AVAILABLE, PerformanceSeatState.RESERVED);
    }
}
