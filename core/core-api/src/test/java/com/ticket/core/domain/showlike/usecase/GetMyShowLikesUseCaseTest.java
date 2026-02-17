package com.ticket.core.domain.showlike.usecase;

import com.ticket.core.api.controller.response.ShowLikeSummaryResponse;
import com.ticket.core.domain.showlike.ShowLikeQueryRepository;
import com.ticket.core.support.cursor.CursorSlice;
import com.ticket.core.support.exception.CoreException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetMyShowLikesUseCaseTest {

    @InjectMocks
    private GetMyShowLikesUseCase getMyShowLikesUseCase;

    @Mock
    private ShowLikeQueryRepository showLikeQueryRepository;

    @Test
    void 잘못된_cursor면_예외가_발생한다() {
        // when / then
        assertThatThrownBy(() -> getMyShowLikesUseCase.execute(
                new GetMyShowLikesUseCase.Input(1L, "invalid-cursor", 20)
        )).isInstanceOf(CoreException.class);
    }

    @Test
    void size가_허용_범위를_벗어나면_예외가_발생한다() {
        // when / then
        assertThatThrownBy(() -> getMyShowLikesUseCase.execute(
                new GetMyShowLikesUseCase.Input(1L, null, 0)
        )).isInstanceOf(CoreException.class);
    }

    @Test
    void 정상_요청이면_찜_목록과_nextCursor를_반환한다() {
        // given
        final ShowLikeSummaryResponse response = new ShowLikeSummaryResponse(
                10L,
                "테스트 공연",
                "https://example.com/test.jpg",
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 2),
                "테스트 공연장",
                LocalDateTime.of(2026, 2, 17, 10, 0)
        );
        final Slice<ShowLikeSummaryResponse> slice =
                new SliceImpl<>(List.of(response), PageRequest.of(0, 20), true);
        when(showLikeQueryRepository.findMyLikedShows(1L, 123L, 20))
                .thenReturn(new CursorSlice<>(slice, "122"));

        // when
        final GetMyShowLikesUseCase.Output output = getMyShowLikesUseCase.execute(
                new GetMyShowLikesUseCase.Input(1L, "123", 20)
        );

        // then
        assertThat(output.shows().getContent()).hasSize(1);
        assertThat(output.shows().getContent().getFirst().title()).isEqualTo("테스트 공연");
        assertThat(output.nextCursor()).isEqualTo("122");
    }
}
