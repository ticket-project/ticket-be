package com.ticket.core.domain.show.query;

import com.ticket.core.domain.show.meta.Region;
import com.ticket.core.domain.show.query.ShowListQueryRepository;
import com.ticket.core.domain.show.query.model.ShowSearchCriteria;
import com.ticket.core.support.cursor.CursorSlice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.SliceImpl;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
class SearchShowsUseCaseTest {

    @Mock
    private ShowListQueryRepository showListQueryRepository;

    @InjectMocks
    private SearchShowsUseCase useCase;

    @Test
    void 검색_결과와_커서를_반환한다() {
        ShowSearchCriteria request = new ShowSearchCriteria("공연", null, null, null, null, null, null);
        SearchShowsUseCase.ShowSearchItem item = new SearchShowsUseCase.ShowSearchItem(
                1L,
                "공연",
                "image",
                "장소",
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                Region.SEOUL,
                10L
        );
        CursorSlice<SearchShowsUseCase.ShowSearchItem> result = new CursorSlice<>(new SliceImpl<>(List.of(item)), "next");
        when(showListQueryRepository.searchShows(request, 20, "popular")).thenReturn(result);

        SearchShowsUseCase.Output output = useCase.execute(new SearchShowsUseCase.Input(request, 20, ShowSort.from("popular")));

        assertThat(output.shows().getContent()).containsExactly(item);
        assertThat(output.nextCursor()).isEqualTo("next");
        verify(showListQueryRepository).searchShows(request, 20, "popular");
    }

    @Test
    void 검색_결과가_없으면_빈_슬라이스와_null_커서를_반환한다() {
        ShowSearchCriteria request = new ShowSearchCriteria("없는공연", null, null, null, null, null, null);
        CursorSlice<SearchShowsUseCase.ShowSearchItem> result = new CursorSlice<>(new SliceImpl<>(List.of()), null);
        when(showListQueryRepository.searchShows(request, 20, "popular")).thenReturn(result);

        SearchShowsUseCase.Output output = useCase.execute(new SearchShowsUseCase.Input(request, 20, ShowSort.from("popular")));

        assertThat(output.shows().getContent()).isEmpty();
        assertThat(output.nextCursor()).isNull();
        verify(showListQueryRepository).searchShows(request, 20, "popular");
    }
}
