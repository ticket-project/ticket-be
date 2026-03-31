package com.ticket.core.domain.performance.query;

import com.ticket.core.domain.performance.model.Performance;
import com.ticket.core.domain.show.model.Show;
import com.ticket.core.domain.show.meta.Region;
import com.ticket.core.domain.show.venue.Venue;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class GetPerformanceSummaryUseCaseTest {

    @Mock
    private PerformanceFinder performanceFinder;

    @InjectMocks
    private GetPerformanceSummaryUseCase useCase;

    @Test
    void 공연_요약정보를_반환한다() {
        //given
        Performance performance = mock(Performance.class);
        Show show = mock(Show.class);
        Venue venue = mock(Venue.class);
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 20, 19, 30);

        when(performanceFinder.findById(1L)).thenReturn(performance);
        when(performance.getShow()).thenReturn(show);
        when(performance.getStartTime()).thenReturn(startTime);
        when(performance.getMaxCanHoldCount()).thenReturn(4);
        when(show.getTitle()).thenReturn("싱어게인");
        when(show.getVenue()).thenReturn(venue);
        when(venue.getRegion()).thenReturn(Region.CHUNGCHEONG);

        //when
        GetPerformanceSummaryUseCase.Output output = useCase.execute(new GetPerformanceSummaryUseCase.Input(1L));

        //then
        assertThat(output.title()).isEqualTo("싱어게인");
        assertThat(output.region()).isEqualTo("충청");
        assertThat(output.startTime()).isEqualTo(startTime);
        assertThat(output.maxCanHoldCount()).isEqualTo(4);
    }

    @Test
    void 공연과_연결되지_않은_회차면_예외를_던진다() {
        //given
        Performance performance = mock(Performance.class);
        when(performanceFinder.findById(1L)).thenReturn(performance);
        when(performance.getShow()).thenReturn(null);

        //when
        //then
        assertThatThrownBy(() -> useCase.execute(new GetPerformanceSummaryUseCase.Input(1L)))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> assertThat(((CoreException) exception).getErrorType()).isEqualTo(ErrorType.NOT_FOUND_DATA));
    }

    @Test
    void 공연장이_없어도_지역은_null로_반환한다() {
        //given
        Performance performance = mock(Performance.class);
        Show show = mock(Show.class);
        LocalDateTime startTime = LocalDateTime.of(2026, 3, 20, 19, 30);

        when(performanceFinder.findById(1L)).thenReturn(performance);
        when(performance.getShow()).thenReturn(show);
        when(performance.getStartTime()).thenReturn(startTime);
        when(performance.getMaxCanHoldCount()).thenReturn(null);
        when(show.getTitle()).thenReturn("싱어게인");
        when(show.getVenue()).thenReturn(null);

        //when
        GetPerformanceSummaryUseCase.Output output = useCase.execute(new GetPerformanceSummaryUseCase.Input(1L));

        //then
        assertThat(output.title()).isEqualTo("싱어게인");
        assertThat(output.region()).isNull();
        assertThat(output.startTime()).isEqualTo(startTime);
        assertThat(output.maxCanHoldCount()).isNull();
    }
}
