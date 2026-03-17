package com.ticket.core.domain.performanceseat.query;

import com.ticket.core.api.controller.response.SeatStatusResponse;
import com.ticket.core.api.controller.response.ShowSeatResponse;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.seat.Seat;
import com.ticket.core.domain.show.Show;
import com.ticket.core.domain.show.mapping.ShowGrade;
import com.ticket.core.domain.show.meta.Region;
import com.ticket.core.domain.show.venue.Venue;
import com.ticket.core.domain.support.QueryRepositoryTestSupport;
import com.ticket.core.enums.PerformanceSeatState;
import com.ticket.core.domain.performanceseat.query.model.SeatStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
        Venue venue = persistVenue("공연장", Region.SEOUL);
        Show show = persistShow("공연", venue, null, 10L, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(5));
        showId = show.getId();
        ShowGrade vip = persistShowGrade(show, "VIP", "VIP석", BigDecimal.valueOf(150000), 1);
        ShowGrade r = persistShowGrade(show, "R", "R석", BigDecimal.valueOf(100000), 2);
        Seat seat1 = persistSeat("A", "01", "02", 1);
        Seat seat2 = persistSeat("A", "01", "01", 1);
        persistShowSeat(show, seat1, r);
        persistShowSeat(show, seat2, vip);

        Performance performance = persistPerformance(show, 1L, LocalDateTime.now().plusDays(1));
        performanceId = performance.getId();
        persistPerformanceSeat(performance, seat1, PerformanceSeatState.RESERVED, BigDecimal.valueOf(100000));
        persistPerformanceSeat(performance, seat2, PerformanceSeatState.AVAILABLE, BigDecimal.valueOf(150000));
        flushAndClear();
    }

    @Test
    void 공연_좌석정보를_정렬해서_조회한다() {
        //given
        //when
        List<ShowSeatResponse.SeatInfo> result = seatMapQueryRepository.findShowSeats(showId);

        //then
        assertThat(result).extracting("seatId").hasSize(2);
        assertThat(result).extracting("gradeCode").containsExactly("VIP", "R");
        assertThat(result).extracting("col").containsExactly("01", "02");
    }

    @Test
    void 회차별_좌석상태를_API_상태로_변환한다() {
        //given
        //when
        List<SeatStatusResponse.SeatState> result = seatMapQueryRepository.findSeatStatuses(performanceId);

        //then
        assertThat(result).containsExactly(
                new SeatStatusResponse.SeatState(result.get(0).seatId(), SeatStatus.OCCUPIED),
                new SeatStatusResponse.SeatState(result.get(1).seatId(), SeatStatus.AVAILABLE)
        );
    }
}

