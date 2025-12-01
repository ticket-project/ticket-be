package com.ticket.performance;

import com.ticket.core.domain.performance.PerformanceService;
import com.ticket.core.domain.seat.Seat;
import com.ticket.core.support.exception.CoreException;
import com.ticket.storage.db.core.PerformanceRepository;
import com.ticket.storage.db.core.SeatEntity;
import com.ticket.storage.db.core.SeatRepository;
import com.ticket.storage.db.core.SeatStatus;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
public class PerformanceServiceTest {

    @InjectMocks
    private PerformanceService performanceService;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private PerformanceRepository performanceRepository;

    @Test
    void 회차의_재고가_0이하라면_예매_가능한_전체_좌석_조회는_실패한다() {
        //given
        Long performanceId = 1L;
        SeatStatus status = SeatStatus.AVAILABLE;
        when(seatRepository.countByPerformanceIdAndStatus(performanceId, status)).thenReturn(0L);
        //then
        Assertions.assertThatThrownBy(() -> performanceService.findAllSeatByPerformance(performanceId)).isInstanceOf(CoreException.class);
    }

    @Test
    void 회차의_재고가_1이상이라면_예매_가능한_전체_좌석_조회는_성공한다() {
        //given
        Long performanceId = 1L;
        SeatStatus status = SeatStatus.AVAILABLE;
        final List<SeatEntity> seats = List.of(new SeatEntity(1L, performanceId, "x1", "y1", SeatStatus.AVAILABLE), new SeatEntity(2L, performanceId, "x2", "y2", SeatStatus.AVAILABLE));
        when(seatRepository.findByPerformanceId(performanceId)).thenReturn(seats);
        when(seatRepository.countByPerformanceIdAndStatus(performanceId, status)).thenReturn(1L);
        //when
        List<Seat> seatList = performanceService.findAllSeatByPerformance(performanceId);
        //then
        Assertions.assertThat(seatList).hasSize(2);
        seatList.forEach(seat -> Assertions.assertThat(seat.getPerformanceId()).isEqualTo(performanceId));
    }

}
