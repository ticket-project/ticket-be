package com.ticket.core.domain.show.query.usecase;

import com.ticket.core.api.controller.response.ShowSummaryResponse;
import com.ticket.core.domain.show.query.ShowListQueryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetLatestShowsUseCaseTest {

    @Mock
    private ShowListQueryRepository showListQueryRepository;

    @InjectMocks
    private GetLatestShowsUseCase useCase;

    @Test
    void 최신_공연_최대_10개를_조회한다() {
        List<ShowSummaryResponse> responses = List.of(
                new ShowSummaryResponse(1L, "공연", "image", LocalDate.now(), LocalDate.now().plusDays(1), "장소", LocalDateTime.now())
        );
        when(showListQueryRepository.findLatestShows("CONCERT", GetLatestShowsUseCase.LATEST_SHOWS_MAX_COUNT)).thenReturn(responses);

        GetLatestShowsUseCase.Output output = useCase.execute(new GetLatestShowsUseCase.Input("CONCERT"));

        assertThat(output.shows()).isEqualTo(responses);
        verify(showListQueryRepository).findLatestShows("CONCERT", GetLatestShowsUseCase.LATEST_SHOWS_MAX_COUNT);
    }

    @Test
    void 최신_공연이_없으면_빈_목록을_반환한다() {
        when(showListQueryRepository.findLatestShows("CONCERT", GetLatestShowsUseCase.LATEST_SHOWS_MAX_COUNT))
                .thenReturn(List.of());

        GetLatestShowsUseCase.Output output = useCase.execute(new GetLatestShowsUseCase.Input("CONCERT"));

        assertThat(output.shows()).isEmpty();
        verify(showListQueryRepository).findLatestShows("CONCERT", GetLatestShowsUseCase.LATEST_SHOWS_MAX_COUNT);
    }
}
