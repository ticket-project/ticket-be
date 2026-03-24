package com.ticket.core.domain.order.create;

import com.ticket.core.domain.hold.model.HoldSnapshot;
import com.ticket.core.domain.hold.support.HoldManager;
import com.ticket.core.domain.hold.support.HoldSeatAvailabilityValidator;
import com.ticket.core.domain.performanceseat.model.PerformanceSeat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
class HoldAllocatorTest {

    @Mock
    private HoldSeatAvailabilityValidator holdSeatAvailabilityValidator;

    @Mock
    private HoldManager holdManager;

    @InjectMocks
    private HoldAllocator holdAllocator;

    @Test
    void 좌석을_검증하고_hold를_확보한다() {
        RequestedSeatIds seatIds = RequestedSeatIds.from(List.of(3L, 7L));
        Duration holdDuration = Duration.ofMinutes(10);
        List<PerformanceSeat> seats = List.of(mock(PerformanceSeat.class), mock(PerformanceSeat.class));
        HoldSnapshot snapshot = new HoldSnapshot(
                "hold-key",
                20L,
                10L,
                seatIds.values(),
                LocalDateTime.of(2026, 3, 15, 12, 0)
        );

        when(holdSeatAvailabilityValidator.validate(10L, seatIds.values())).thenReturn(seats);
        when(holdManager.createHold(20L, 10L, seatIds.values(), holdDuration)).thenReturn(snapshot);

        HoldAllocation allocation = holdAllocator.allocate(20L, 10L, seatIds, holdDuration);

        assertThat(allocation.snapshot()).isEqualTo(snapshot);
        assertThat(allocation.performanceSeats()).isEqualTo(seats);
    }

    @Test
    void 확보한_hold를_해제한다() {
        List<Long> seatIds = List.of(3L, 7L);
        HoldSnapshot snapshot = new HoldSnapshot(
                "hold-key",
                20L,
                10L,
                seatIds,
                LocalDateTime.of(2026, 3, 15, 12, 0)
        );
        HoldAllocation allocation = new HoldAllocation(snapshot, List.of(mock(PerformanceSeat.class)));

        holdAllocator.release(allocation);

        verify(holdManager).release(10L, "hold-key", seatIds);
    }
}
