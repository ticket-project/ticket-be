package com.ticket.core.domain.show.usecase;

import com.ticket.core.api.controller.response.ShowSummaryResponse;
import com.ticket.core.domain.show.ShowListQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetLatestShowsUseCase {
    public static final int LATEST_SHOWS_MAX_COUNT = 10;
    private final ShowListQueryRepository showListQueryRepository;

    public GetLatestShowsUseCase(final ShowListQueryRepository showListQueryRepository) {
        this.showListQueryRepository = showListQueryRepository;
    }

    public record Input(String category) {
    }

    public record Output(List<ShowSummaryResponse> shows) {
    }

    public Output execute(final Input input) {
        return new Output(showListQueryRepository.findLatestShows(input.category, LATEST_SHOWS_MAX_COUNT));
    }
}
