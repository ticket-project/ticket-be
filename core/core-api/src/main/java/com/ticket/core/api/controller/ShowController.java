package com.ticket.core.api.controller;

import com.ticket.core.api.controller.docs.ShowControllerDocs;
import com.ticket.core.api.controller.request.SaleOpeningSoonSearchParam;
import com.ticket.core.api.controller.request.ShowParam;
import com.ticket.core.api.controller.request.ShowSearchRequest;
import com.ticket.core.api.controller.response.ShowDetailResponse;
import com.ticket.core.api.controller.response.ShowOpeningSoonDetailResponse;
import com.ticket.core.api.controller.response.ShowResponse;
import com.ticket.core.api.controller.response.ShowSearchCountResponse;
import com.ticket.core.api.controller.response.ShowSearchResponse;
import com.ticket.core.domain.show.usecase.*;
import com.ticket.core.support.response.ApiResponse;
import com.ticket.core.support.response.SliceResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shows")
@Validated
public class ShowController implements ShowControllerDocs {

    private final GetShowsUseCase getShowsUseCase;
    private final GetLatestShowsUseCase getLatestShowsUseCase;
    private final GetSaleStartApproachingShowsUseCase getSaleStartApproachingShowsUseCase;
    private final GetSaleStartApproachingShowsPageUseCase getSaleStartApproachingShowsPageUseCase;
    private final SearchShowsUseCase searchShowsUseCase;
    private final CountSearchShowsUseCase countSearchShowsUseCase;
    private final GetShowDetailUseCase getShowDetailUseCase;

    public ShowController(
            final GetShowsUseCase getShowsUseCase,
            final GetLatestShowsUseCase getLatestShowsUseCase,
            final GetSaleStartApproachingShowsUseCase getSaleStartApproachingShowsUseCase,
            final GetSaleStartApproachingShowsPageUseCase getSaleStartApproachingShowsPageUseCase,
            final SearchShowsUseCase searchShowsUseCase,
            final CountSearchShowsUseCase countSearchShowsUseCase,
            final GetShowDetailUseCase getShowDetailUseCase
    ) {
        this.getShowsUseCase = getShowsUseCase;
        this.getLatestShowsUseCase = getLatestShowsUseCase;
        this.getSaleStartApproachingShowsUseCase = getSaleStartApproachingShowsUseCase;
        this.getSaleStartApproachingShowsPageUseCase = getSaleStartApproachingShowsPageUseCase;
        this.searchShowsUseCase = searchShowsUseCase;
        this.countSearchShowsUseCase = countSearchShowsUseCase;
        this.getShowDetailUseCase = getShowDetailUseCase;
    }

    @Override
    @GetMapping("/{id}")
    public ApiResponse<ShowDetailResponse> getShowDetail(@PathVariable final Long id) {
        final GetShowDetailUseCase.Input input = new GetShowDetailUseCase.Input(id);
        final GetShowDetailUseCase.Output output = getShowDetailUseCase.execute(input);
        return ApiResponse.success(output.show());
    }

    @Override
    @GetMapping
    public ApiResponse<SliceResponse<ShowResponse>> getShowsPage(
            @ParameterObject final ShowParam param,
            @RequestParam(defaultValue = "5") final int size,
            @RequestParam(defaultValue = "popular") final String sort
    ) {
        final GetShowsUseCase.Input input = new GetShowsUseCase.Input(param, size, sort);
        final GetShowsUseCase.Output output = getShowsUseCase.execute(input);
        return ApiResponse.success(SliceResponse.from(output.shows(), output.nextCursor()));
    }

    @Override
    @GetMapping("/latest")
    public ApiResponse<GetLatestShowsUseCase.Output> getLatestShows(
            @RequestParam(defaultValue = "CONCERT") final String category
    ) {
        GetLatestShowsUseCase.Input input = new GetLatestShowsUseCase.Input(category);
        return ApiResponse.success(getLatestShowsUseCase.execute(input));
    }

    @Override
    @GetMapping("/sale-opening-soon")
    public ApiResponse<GetSaleStartApproachingShowsUseCase.Output> getShowsSaleOpeningSoon(
            @RequestParam(defaultValue = "CONCERT") final String category,
            @RequestParam(defaultValue = "5") final int size
    ) {
        GetSaleStartApproachingShowsUseCase.Input input = new GetSaleStartApproachingShowsUseCase.Input(category, size);
        return ApiResponse.success(getSaleStartApproachingShowsUseCase.execute(input));
    }

    @Override
    @GetMapping("/sale-opening-soon/page")
    public ApiResponse<SliceResponse<ShowOpeningSoonDetailResponse>> getShowsSaleOpeningSoonPage(
            @ParameterObject final SaleOpeningSoonSearchParam param,
            @RequestParam(defaultValue = "16") final int size,
            @RequestParam(defaultValue = "saleStartApproaching") final String sort
    ) {
        final GetSaleStartApproachingShowsPageUseCase.Input input = new GetSaleStartApproachingShowsPageUseCase.Input(param, size, sort);
        final GetSaleStartApproachingShowsPageUseCase.Output output = getSaleStartApproachingShowsPageUseCase.execute(input);
        return ApiResponse.success(SliceResponse.from(output.shows(), output.nextCursor()));
    }

    @Override
    @GetMapping("/search")
    public ApiResponse<SliceResponse<ShowSearchResponse>> searchShows(
            @ParameterObject final ShowSearchRequest request,
            @RequestParam(defaultValue = "20") final int size,
            @RequestParam(defaultValue = "popular") final String sort
    ) {
        final SearchShowsUseCase.Input input = new SearchShowsUseCase.Input(request, size, sort);
        final SearchShowsUseCase.Output output = searchShowsUseCase.execute(input);
        return ApiResponse.success(SliceResponse.from(output.shows(), output.nextCursor()));
    }

    @Override
    @GetMapping("/search/count")
    public ApiResponse<ShowSearchCountResponse> countSearchShows(
            @ParameterObject final ShowSearchRequest request
    ) {
        final CountSearchShowsUseCase.Input input = new CountSearchShowsUseCase.Input(request);
        final CountSearchShowsUseCase.Output output = countSearchShowsUseCase.execute(input);
        return ApiResponse.success(output.response());
    }
}
