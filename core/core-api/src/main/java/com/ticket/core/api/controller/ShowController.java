package com.ticket.core.api.controller;

import com.ticket.core.api.controller.request.ShowSearchParam;
import com.ticket.core.domain.show.usecase.SearchShowsUseCase;
import com.ticket.core.support.response.ApiResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shows")
public class ShowController {
    private final SearchShowsUseCase searchShowsUseCase;

    public ShowController(final SearchShowsUseCase searchShowsUseCase) {
        this.searchShowsUseCase = searchShowsUseCase;
    }

    @GetMapping("/search")
    public ApiResponse<Void> searchShows(ShowSearchParam param, Pageable pageable) {
        SearchShowsUseCase.Input input = new SearchShowsUseCase.Input(param, pageable);
        searchShowsUseCase.execute(input);
        return null;
    }

}
