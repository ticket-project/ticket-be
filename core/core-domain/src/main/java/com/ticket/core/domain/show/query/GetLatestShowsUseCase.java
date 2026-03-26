package com.ticket.core.domain.show.query;

import com.ticket.core.domain.show.query.ShowListQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetLatestShowsUseCase {
    public static final int LATEST_SHOWS_MAX_COUNT = 10;
    private final ShowListQueryRepository showListQueryRepository;

    public record Input(String category) {
    }

    public record Output(List<ShowSummary> shows) {
    }

    public record ShowSummary(
            Long id,
            String title,
            String image,
            LocalDate startDate,
            LocalDate endDate,
            String venue,
            LocalDateTime createdAt
    ) {
    }

    public Output execute(final Input input) {
        return new Output(showListQueryRepository.findLatestShows(input.category, LATEST_SHOWS_MAX_COUNT));
    }
}
