package com.ticket.core.domain.show.query.usecase;

import com.ticket.core.api.controller.response.ShowOpeningSoonSummaryResponse;
import com.ticket.core.domain.show.query.ShowListQueryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
class GetSaleStartApproachingShowsUseCaseTest {

    @Mock
    private ShowListQueryRepository showListQueryRepository;
    @InjectMocks
    private GetSaleStartApproachingShowsUseCase useCase;

    @Test
    void 판매오픈임박_공연_목록을_반환한다() {
        List<ShowOpeningSoonSummaryResponse> shows = List.of(
                new ShowOpeningSoonSummaryResponse(1L, "공연", "image", "장소", LocalDateTime.now())
        );
        when(showListQueryRepository.findShowsSaleOpeningSoon("CONCERT", 5)).thenReturn(shows);

        GetSaleStartApproachingShowsUseCase.Output output = useCase.execute(new GetSaleStartApproachingShowsUseCase.Input("CONCERT", 5));

        assertThat(output.shows()).isEqualTo(shows);
        verify(showListQueryRepository).findShowsSaleOpeningSoon("CONCERT", 5);
    }

    @Test
    void 판매오픈임박_공연이_없으면_빈_목록을_반환한다() {
        when(showListQueryRepository.findShowsSaleOpeningSoon("CONCERT", 5)).thenReturn(List.of());

        GetSaleStartApproachingShowsUseCase.Output output =
                useCase.execute(new GetSaleStartApproachingShowsUseCase.Input("CONCERT", 5));

        assertThat(output.shows()).isEmpty();
        verify(showListQueryRepository).findShowsSaleOpeningSoon("CONCERT", 5);
    }
}
