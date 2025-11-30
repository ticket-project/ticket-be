package com.ticket.performance;

import com.ticket.core.domain.performance.PerformanceService;
import com.ticket.core.support.exception.CoreException;
import com.ticket.storage.db.core.SeatRepository;
import com.ticket.storage.db.core.SeatStatus;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
public class PerformanceServiceTest {

    @InjectMocks
    private PerformanceService performanceService;

    @Mock
    private SeatRepository seatRepository;

    @Test
    void 회차의_재고가_0이하라면_예매_가능한_전체_좌석_조회는_실패한다() {
        //given
        Long performanceId = 1L;
        SeatStatus status = SeatStatus.AVAILABLE;
        when(seatRepository.countByPerformanceIdAndStatus(performanceId, status)).thenReturn(0L);
        //then
        Assertions.assertThatThrownBy(() -> performanceService.findAllSeatByPerformance(performanceId)).isInstanceOf(CoreException.class);
    }

}
