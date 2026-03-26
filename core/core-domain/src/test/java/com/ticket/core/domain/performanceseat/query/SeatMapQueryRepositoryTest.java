package com.ticket.core.domain.performanceseat.query;

import com.ticket.core.domain.performanceseat.query.model.SeatStatus;
import com.ticket.core.domain.performance.model.Performance;
import com.ticket.core.domain.seat.model.Seat;
import com.ticket.core.domain.show.model.Show;
import com.ticket.core.domain.show.mapping.ShowGrade;
import com.ticket.core.domain.show.meta.Region;
import com.ticket.core.domain.show.venue.Venue;
import com.ticket.core.domain.support.QueryRepositoryTestSupport;
import com.ticket.core.domain.performanceseat.model.PerformanceSeatState;
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
    void 공연_좌석_정보를_정렬해서_조회한다() {
        List<GetShowSeatsUseCase.SeatInfo> result = seatMapQueryRepository.findShowSeats(showId);

        assertThat(result).extracting(GetShowSeatsUseCase.SeatInfo::seatId).hasSize(2);
        assertThat(result).extracting(GetShowSeatsUseCase.SeatInfo::gradeCode).containsExactly("VIP", "R");
        assertThat(result).extracting(GetShowSeatsUseCase.SeatInfo::col).containsExactly("01", "02");
    }

    @Test
    void 좌석별_상태를_api_상태로_변환한다() {
        List<GetSeatStatusUseCase.SeatState> result = seatMapQueryRepository.findSeatStatuses(performanceId);

        assertThat(result).containsExactly(
                new GetSeatStatusUseCase.SeatState(result.get(0).seatId(), SeatStatus.OCCUPIED),
                new GetSeatStatusUseCase.SeatState(result.get(1).seatId(), SeatStatus.AVAILABLE)
        );
    }
}
