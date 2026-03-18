package com.ticket.core.domain.show.query.usecase;

import com.ticket.core.domain.show.query.ShowListQueryRepository;
import com.ticket.core.domain.show.query.model.ShowSearchRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
class CountSearchShowsUseCaseTest {

    @Mock
    private ShowListQueryRepository showListQueryRepository;

    @InjectMocks
    private CountSearchShowsUseCase useCase;

    @Test
    void 검색_개수를_응답으로_감싼다() {
        //given
        ShowSearchRequest request = new ShowSearchRequest("뮤지컬", null, null, null, null, null, null);
        when(showListQueryRepository.countSearchShows(request)).thenReturn(42L);

        //when
        CountSearchShowsUseCase.Output output = useCase.execute(new CountSearchShowsUseCase.Input(request));

        //then
        assertThat(output.count()).isEqualTo(42L);
        verify(showListQueryRepository).countSearchShows(request);
    }

    @Test
    void 검색결과가_없으면_0건을_반환한다() {
        //given
        ShowSearchRequest request = new ShowSearchRequest("없는공연", null, null, null, null, null, null);
        when(showListQueryRepository.countSearchShows(request)).thenReturn(0L);

        //when
        CountSearchShowsUseCase.Output output = useCase.execute(new CountSearchShowsUseCase.Input(request));

        //then
        assertThat(output.count()).isZero();
        verify(showListQueryRepository).countSearchShows(request);
    }
}
