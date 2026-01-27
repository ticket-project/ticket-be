package com.ticket.core.domain.show.usecase;

import com.ticket.core.api.controller.response.ShowSummaryResponse;
import com.ticket.core.domain.show.ShowQuerydslRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetLatestShowsUseCase {
    public static final int LATEST_SHOWS_MAX_COUNT = 10;
    private final ShowQuerydslRepository showQuerydslRepository;

    public GetLatestShowsUseCase(final ShowQuerydslRepository showQuerydslRepository) {
        this.showQuerydslRepository = showQuerydslRepository;
    }

    public record Input(String category) {
    }

    public record Output(List<ShowSummaryResponse> shows) {
    }

    public Output execute(final Input input) {
        return new Output(showQuerydslRepository.findLatestShows(input.category, LATEST_SHOWS_MAX_COUNT));
    }
}
