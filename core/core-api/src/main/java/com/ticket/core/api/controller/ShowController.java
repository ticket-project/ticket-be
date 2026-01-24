package com.ticket.core.api.controller;

import com.ticket.core.api.controller.request.ShowSearchParam;
import com.ticket.core.api.controller.response.ShowResponse;
import com.ticket.core.domain.show.usecase.SearchShowsUseCase;
import com.ticket.core.support.response.ApiResponse;
import com.ticket.core.support.response.SliceResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    public ApiResponse<SliceResponse<ShowResponse>> searchShows(final ShowSearchParam param, @PageableDefault(size = 5) final Pageable pageable) {
        final SearchShowsUseCase.Input input = new SearchShowsUseCase.Input(param, pageable);
        final SearchShowsUseCase.Output output = searchShowsUseCase.execute(input);
        return ApiResponse.success(SliceResponse.from(output.shows(), output.nextCursor()));
    }

}
