package com.ticket.core.domain.performance;

import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import com.ticket.core.support.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class PerformanceFinderTest {

    @Mock
    private PerformanceRepository performanceRepository;

    @InjectMocks
    private PerformanceFinder performanceFinder;

    @Test
    void 예약중인_공연이면_그대로_반환한다() {
        Performance performance = createPerformance(
                LocalDateTime.now().minusMinutes(10),
                LocalDateTime.now().plusMinutes(10)
        );
        when(performanceRepository.findById(1L)).thenReturn(Optional.of(performance));

        Performance result = performanceFinder.findOpenPerformance(1L);

        assertThat(result).isSameAs(performance);
    }

    @Test
    void 예약중이_아니면_찾을수없음_예외를_던진다() {
        Performance performance = createPerformance(
                LocalDateTime.now().plusMinutes(10),
                LocalDateTime.now().plusMinutes(20)
        );
        when(performanceRepository.findById(1L)).thenReturn(Optional.of(performance));

        assertThatThrownBy(() -> performanceFinder.findOpenPerformance(1L))
                .isInstanceOf(NotFoundException.class)
                .satisfies(thrown -> assertThat(((NotFoundException) thrown).getErrorType()).isEqualTo(ErrorType.NOT_FOUND_DATA));
    }

    @Test
    void 공연이_없으면_findById가_찾을수없음_예외를_던진다() {
        when(performanceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> performanceFinder.findById(1L))
                .isInstanceOf(CoreException.class)
                .satisfies(thrown -> assertThat(((CoreException) thrown).getErrorType()).isEqualTo(ErrorType.NOT_FOUND_DATA));
    }

    @Test
    void 예매중이지만_아직_예매시간_전이면_예외를_던진다() {
        Performance performance = createPerformance(
                LocalDateTime.now().plusMinutes(10),
                LocalDateTime.now().plusMinutes(20)
        );
        when(performanceRepository.findById(1L)).thenReturn(Optional.of(performance));

        assertThatThrownBy(() -> performanceFinder.findValidPerformanceById(1L))
                .isInstanceOf(CoreException.class)
                .satisfies(thrown -> assertThat(((CoreException) thrown).getErrorType()).isEqualTo(ErrorType.NOT_YET_RESERVE_TIME));
    }

    @Test
    void 예매시작시간이_null이면_아직_예매시간_전_예외를_던진다() {
        Performance performance = createPerformance(
                null,
                LocalDateTime.now().plusMinutes(20)
        );
        when(performanceRepository.findById(1L)).thenReturn(Optional.of(performance));

        assertThatThrownBy(() -> performanceFinder.findValidPerformanceById(1L))
                .isInstanceOf(CoreException.class)
                .satisfies(thrown -> assertThat(((CoreException) thrown).getErrorType()).isEqualTo(ErrorType.NOT_YET_RESERVE_TIME));
    }

    @Test
    void 예매마감된_공연은_종료_예외를_던진다() {
        Performance performance = createPerformance(
                LocalDateTime.now().minusMinutes(20),
                LocalDateTime.now().minusMinutes(10)
        );
        when(performanceRepository.findById(1L)).thenReturn(Optional.of(performance));

        assertThatThrownBy(() -> performanceFinder.findValidPerformanceById(1L))
                .isInstanceOf(CoreException.class)
                .satisfies(thrown -> assertThat(((CoreException) thrown).getErrorType()).isEqualTo(ErrorType.PERFORMANCE_IS_PAST));
    }

    @Test
    void 예매마감시간이_null이면_종료_예외를_던진다() {
        Performance performance = createPerformance(
                LocalDateTime.now().minusMinutes(20),
                null
        );
        when(performanceRepository.findById(1L)).thenReturn(Optional.of(performance));

        assertThatThrownBy(() -> performanceFinder.findValidPerformanceById(1L))
                .isInstanceOf(CoreException.class)
                .satisfies(thrown -> assertThat(((CoreException) thrown).getErrorType()).isEqualTo(ErrorType.PERFORMANCE_IS_PAST));
    }

    @Test
    void 예매가능한_공연이면_findValidPerformanceById가_반환한다() {
        Performance performance = createPerformance(
                LocalDateTime.now().minusMinutes(10),
                LocalDateTime.now().plusMinutes(10)
        );
        when(performanceRepository.findById(1L)).thenReturn(Optional.of(performance));

        Performance result = performanceFinder.findValidPerformanceById(1L);

        assertThat(result).isSameAs(performance);
    }

    private Performance createPerformance(final LocalDateTime orderOpenTime, final LocalDateTime orderCloseTime) {
        return new Performance(
                null,
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2),
                orderOpenTime,
                orderCloseTime,
                4,
                300
        );
    }
}
