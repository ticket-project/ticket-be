package com.ticket.core.domain.commoncode.usecase;

import com.ticket.core.api.controller.response.MetaCodesResponse;
import com.ticket.core.domain.show.category.Category;
import com.ticket.core.domain.show.category.CategoryRepository;
import com.ticket.core.domain.show.genre.Genre;
import com.ticket.core.domain.show.genre.GenreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
class GetMetaCodesUseCaseTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private GenreRepository genreRepository;

    @Test
    void 카테고리_장르_및_enum_코드를_모아_반환한다() {
        Category category = mock(Category.class);
        Genre genre = mock(Genre.class);

        when(category.getId()).thenReturn(1L);
        when(category.getCode()).thenReturn("CONCERT");
        when(category.getName()).thenReturn("콘서트");

        when(genre.getId()).thenReturn(2L);
        when(genre.getCategory()).thenReturn(category);
        when(genre.getCode()).thenReturn("KPOP");
        when(genre.getName()).thenReturn("케이팝");

        when(categoryRepository.findAllByOrderByIdAsc()).thenReturn(List.of(category));
        when(genreRepository.findAllByOrderByCategory_IdAscNameAsc()).thenReturn(List.of(genre));

        GetMetaCodesUseCase useCase = new GetMetaCodesUseCase(categoryRepository, genreRepository);
        GetMetaCodesUseCase.Output output = useCase.execute();

        MetaCodesResponse response = output.codes();
        assertThat(response.categories()).containsExactly(new MetaCodesResponse.CategoryCodeItem(1L, "CONCERT", "콘서트"));
        assertThat(response.genres()).containsExactly(new MetaCodesResponse.GenreCodeItem(2L, "CONCERT", "KPOP", "케이팝"));
        assertThat(response.enums().bookingStatus()).isNotEmpty();
        assertThat(response.enums().showSortKey()).isNotEmpty();
    }
}
