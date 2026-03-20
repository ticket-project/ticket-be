package com.ticket.core.api.controller;

import com.ticket.core.api.controller.response.ShowResponse;
import com.ticket.core.api.controller.response.ShowSearchResponse;
import com.ticket.core.domain.performanceseat.query.usecase.GetShowSeatsUseCase;
import com.ticket.core.domain.performanceseat.query.usecase.GetVenueLayoutUseCase;
import com.ticket.core.domain.show.meta.Region;
import com.ticket.core.domain.show.meta.SaleType;
import com.ticket.core.domain.show.query.model.ShowParam;
import com.ticket.core.domain.show.query.model.ShowSearchRequest;
import com.ticket.core.domain.show.query.usecase.CountSearchShowsUseCase;
import com.ticket.core.domain.show.query.usecase.GetLatestShowsUseCase;
import com.ticket.core.domain.show.query.usecase.GetSaleStartApproachingShowsPageUseCase;
import com.ticket.core.domain.show.query.usecase.GetSaleStartApproachingShowsUseCase;
import com.ticket.core.domain.show.query.usecase.GetShowDetailUseCase;
import com.ticket.core.domain.show.query.usecase.GetShowsUseCase;
import com.ticket.core.domain.show.query.usecase.SearchShowsUseCase;
import com.ticket.core.domain.show.query.usecase.ShowSort;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("NonAsciiCharacters")
class ShowControllerContractTest {

    @Test
    void 공연_목록_API는_슬라이스_응답_계약을_유지한다() throws Exception {
        GetShowsUseCase getShowsUseCase = mock(GetShowsUseCase.class);
        ShowController controller = new ShowController(
                getShowsUseCase,
                mock(GetLatestShowsUseCase.class),
                mock(GetSaleStartApproachingShowsUseCase.class),
                mock(GetSaleStartApproachingShowsPageUseCase.class),
                mock(SearchShowsUseCase.class),
                mock(CountSearchShowsUseCase.class),
                mock(GetShowDetailUseCase.class),
                mock(GetShowSeatsUseCase.class),
                mock(GetVenueLayoutUseCase.class)
        );
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        ShowResponse show = new ShowResponse(
                1L,
                "공연",
                "부제",
                "image",
                List.of("장르"),
                LocalDate.of(2026, 3, 20),
                LocalDate.of(2026, 3, 21),
                10L,
                SaleType.GENERAL,
                LocalDateTime.of(2026, 3, 19, 10, 0),
                LocalDateTime.of(2026, 3, 21, 10, 0),
                LocalDateTime.of(2026, 3, 18, 10, 0),
                Region.SEOUL,
                "장소"
        );
        when(getShowsUseCase.execute(any(GetShowsUseCase.Input.class)))
                .thenReturn(new GetShowsUseCase.Output(new SliceImpl<>(List.of(show)), "next"));

        mockMvc.perform(get("/api/v1/shows"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.items[0].id").value(1))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andExpect(jsonPath("$.data.size").value(1))
                .andExpect(jsonPath("$.data.numberOfElements").value(1))
                .andExpect(jsonPath("$.data.nextCursor").value("next"))
                .andExpect(jsonPath("$.error").isEmpty());
    }

    @Test
    void 공연_검색_API는_슬라이스_응답_계약을_유지한다() throws Exception {
        SearchShowsUseCase searchShowsUseCase = mock(SearchShowsUseCase.class);
        ShowController controller = new ShowController(
                mock(GetShowsUseCase.class),
                mock(GetLatestShowsUseCase.class),
                mock(GetSaleStartApproachingShowsUseCase.class),
                mock(GetSaleStartApproachingShowsPageUseCase.class),
                searchShowsUseCase,
                mock(CountSearchShowsUseCase.class),
                mock(GetShowDetailUseCase.class),
                mock(GetShowSeatsUseCase.class),
                mock(GetVenueLayoutUseCase.class)
        );
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        ShowSearchRequest request = new ShowSearchRequest("공연", null, null, null, null, null, null);
        ShowSearchResponse response = new ShowSearchResponse(
                1L,
                "공연",
                "image",
                "장소",
                LocalDate.of(2026, 3, 20),
                LocalDate.of(2026, 3, 21),
                Region.SEOUL,
                10L
        );
        when(searchShowsUseCase.execute(any(SearchShowsUseCase.Input.class)))
                .thenReturn(new SearchShowsUseCase.Output(new SliceImpl<>(List.of(response)), "cursor-1"));

        mockMvc.perform(get("/api/v1/shows/search").param("keyword", "공연"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.items[0].id").value(1))
                .andExpect(jsonPath("$.data.nextCursor").value("cursor-1"))
                .andExpect(jsonPath("$.error").isEmpty());
    }
}
