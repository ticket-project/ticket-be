package com.ticket.core.domain.hold.support;

import com.ticket.core.domain.performanceseat.finder.PerformanceSeatFinder;
import com.ticket.core.domain.performanceseat.model.PerformanceSeat;
import com.ticket.core.enums.PerformanceSeatState;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class HoldSeatAvailabilityValidatorTest {

    @Mock
    private PerformanceSeatFinder performanceSeatFinder;

    @InjectMocks
    private HoldSeatAvailabilityValidator validator;

    @Test
    void 좌석개수가_일치하지_않으면_SEAT_MISMATCH_IN_PERFORMANCE_예외를_던진다() {
        PerformanceSeat availableSeat = mock(PerformanceSeat.class);
        when(performanceSeatFinder.findByPerformanceIdAndSeatIdIn(1L, List.of(10L, 20L)))
                .thenReturn(List.of(availableSeat));

        assertThatThrownBy(() -> validator.validate(1L, List.of(10L, 20L)))
                .isInstanceOf(CoreException.class)
                .satisfies(thrown -> assertThat(((CoreException) thrown).getErrorType()).isEqualTo(ErrorType.SEAT_MISMATCH_IN_PERFORMANCE));
    }

    @Test
    void 사용불가_좌석이_포함되면_NOT_EXIST_AVAILABLE_SEAT_예외를_던진다() {
        PerformanceSeat availableSeat = createPerformanceSeat(PerformanceSeatState.AVAILABLE);
        PerformanceSeat reservedSeat = createPerformanceSeat(PerformanceSeatState.RESERVED);
        when(performanceSeatFinder.findByPerformanceIdAndSeatIdIn(1L, List.of(10L, 20L)))
                .thenReturn(List.of(availableSeat, reservedSeat));

        assertThatThrownBy(() -> validator.validate(1L, List.of(10L, 20L)))
                .isInstanceOf(CoreException.class)
                .satisfies(thrown -> assertThat(((CoreException) thrown).getErrorType()).isEqualTo(ErrorType.NOT_EXIST_AVAILABLE_SEAT));
    }

    @Test
    void 모두_예매가능_좌석이면_그대로_반환한다() {
        List<PerformanceSeat> seats = List.of(
                createPerformanceSeat(PerformanceSeatState.AVAILABLE),
                createPerformanceSeat(PerformanceSeatState.AVAILABLE)
        );
        when(performanceSeatFinder.findByPerformanceIdAndSeatIdIn(1L, List.of(10L, 20L))).thenReturn(seats);

        List<PerformanceSeat> result = validator.validate(1L, List.of(10L, 20L));

        assertThat(result).containsExactlyElementsOf(seats);
    }

    private PerformanceSeat createPerformanceSeat(final PerformanceSeatState state) {
        PerformanceSeat performanceSeat = mock(PerformanceSeat.class);
        when(performanceSeat.getState()).thenReturn(state);
        return performanceSeat;
    }
}
