package com.ticket.core.domain.show.query.usecase;

import com.ticket.core.api.controller.response.ShowSearchResponse;
import com.ticket.core.domain.show.meta.Region;
import com.ticket.core.domain.show.query.ShowListQueryRepository;
import com.ticket.core.domain.show.query.model.ShowSearchRequest;
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
        //given
        ShowSearchRequest request = new ShowSearchRequest("공연", null, null, null, null, null, null);
        ShowSearchResponse response =
                new ShowSearchResponse(1L, "공연", "image", "장소", LocalDate.now(), LocalDate.now().plusDays(1), Region.SEOUL, 10L);
        CursorSlice<ShowSearchResponse> result = new CursorSlice<>(new SliceImpl<>(List.of(response)), "next");
        when(showListQueryRepository.searchShows(request, 20, "popular")).thenReturn(result);

        //when
        SearchShowsUseCase.Output output = useCase.execute(new SearchShowsUseCase.Input(request, 20, "popular"));

        //then
        assertThat(output.shows().getContent()).containsExactly(response);
        assertThat(output.nextCursor()).isEqualTo("next");
        verify(showListQueryRepository).searchShows(request, 20, "popular");
    }

    @Test
    void 검색결과가_없으면_빈_슬라이스와_null_커서를_반환한다() {
        //given
        ShowSearchRequest request = new ShowSearchRequest("없는공연", null, null, null, null, null, null);
        CursorSlice<ShowSearchResponse> result = new CursorSlice<>(new SliceImpl<>(List.of()), null);
        when(showListQueryRepository.searchShows(request, 20, "popular")).thenReturn(result);

        //when
        SearchShowsUseCase.Output output = useCase.execute(new SearchShowsUseCase.Input(request, 20, "popular"));

        //then
        assertThat(output.shows().getContent()).isEmpty();
        assertThat(output.nextCursor()).isNull();
        verify(showListQueryRepository).searchShows(request, 20, "popular");
    }
}

