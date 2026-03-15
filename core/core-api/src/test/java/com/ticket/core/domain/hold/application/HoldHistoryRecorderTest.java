package com.ticket.core.domain.hold.application;

import com.ticket.core.domain.hold.model.HoldHistory;
import com.ticket.core.domain.hold.repository.HoldHistoryRepository;
import com.ticket.core.domain.performanceseat.model.PerformanceSeat;
import com.ticket.core.domain.seat.Seat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class HoldHistoryRecorderTest {

    @Mock
    private HoldHistoryRepository holdHistoryRepository;

    @InjectMocks
    private HoldHistoryRecorder holdHistoryRecorder;

    @Test
    void 선택한_좌석마다_active_hold_history를_기록한다() {
        PerformanceSeat first = createPerformanceSeat(100L, 10L);
        PerformanceSeat second = createPerformanceSeat(101L, 20L);
        LocalDateTime expiresAt = LocalDateTime.of(2026, 3, 15, 12, 30);

        holdHistoryRecorder.recordActiveHold(1L, 2L, "hold-key", expiresAt, List.of(first, second));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<HoldHistory>> captor = ArgumentCaptor.forClass(List.class);
        verify(holdHistoryRepository).saveAll(captor.capture());

        List<HoldHistory> histories = captor.getValue();
        assertThat(histories).hasSize(2);
        assertThat(histories.get(0).getHoldKey()).isEqualTo("hold-key");
        assertThat(histories.get(0).getMemberId()).isEqualTo(1L);
        assertThat(histories.get(0).getPerformanceId()).isEqualTo(2L);
        assertThat(histories.get(0).getPerformanceSeatId()).isEqualTo(100L);
        assertThat(histories.get(0).getSeatId()).isEqualTo(10L);
        assertThat(histories.get(0).getExpiresAt()).isEqualTo(expiresAt);
        assertThat(histories.get(1).getPerformanceSeatId()).isEqualTo(101L);
        assertThat(histories.get(1).getSeatId()).isEqualTo(20L);
    }

    private PerformanceSeat createPerformanceSeat(final Long performanceSeatId, final Long seatId) {
        PerformanceSeat performanceSeat = mock(PerformanceSeat.class);
        Seat seat = mock(Seat.class);
        when(performanceSeat.getId()).thenReturn(performanceSeatId);
        when(performanceSeat.getSeat()).thenReturn(seat);
        when(seat.getId()).thenReturn(seatId);
        return performanceSeat;
    }
}
