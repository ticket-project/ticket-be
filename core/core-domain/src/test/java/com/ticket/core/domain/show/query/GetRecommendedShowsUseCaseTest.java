package com.ticket.core.domain.show.query;

import com.ticket.core.domain.member.model.Member;
import com.ticket.core.domain.member.query.MemberFinder;
import com.ticket.core.domain.show.query.model.ShowSummaryView;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class GetRecommendedShowsUseCaseTest {

    @Mock
    private MemberFinder memberFinder;
    @Mock
    private ShowRecommendationQueryRepository showRecommendationQueryRepository;
    @InjectMocks
    private GetRecommendedShowsUseCase useCase;

    @Test
    void 추천_공연_목록을_반환한다() {
        //given
        when(memberFinder.findActiveMemberById(1L)).thenReturn(mock(Member.class));
        final List<ShowSummaryView> expected = List.of(mock(ShowSummaryView.class));
        when(showRecommendationQueryRepository.findRecommendedShows(1L, 10)).thenReturn(expected);

        //when
        GetRecommendedShowsUseCase.Output output = useCase.execute(new GetRecommendedShowsUseCase.Input(1L, 10));

        //then
        assertThat(output.shows()).isEqualTo(expected);
    }

    @Test
    void 추천_공연이_없으면_빈_목록을_반환한다() {
        //given
        when(memberFinder.findActiveMemberById(1L)).thenReturn(mock(Member.class));
        when(showRecommendationQueryRepository.findRecommendedShows(1L, 10)).thenReturn(List.of());

        //when
        GetRecommendedShowsUseCase.Output output = useCase.execute(new GetRecommendedShowsUseCase.Input(1L, 10));

        //then
        assertThat(output.shows()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("invalidInputs")
    void memberId가_없거나_size가_유효하지_않으면_예외를_던진다(final GetRecommendedShowsUseCase.Input input) {
        //given
        //when
        //then
        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> assertThat(((CoreException) exception).getErrorType())
                        .isEqualTo(ErrorType.INVALID_REQUEST));
    }

    private static Stream<Arguments> invalidInputs() {
        return Stream.of(
                Arguments.of((Object) null),
                Arguments.of(new GetRecommendedShowsUseCase.Input(null, 10)),
                Arguments.of(new GetRecommendedShowsUseCase.Input(1L, 0)),
                Arguments.of(new GetRecommendedShowsUseCase.Input(1L, 51))
        );
    }
}
