package com.ticket.core.domain.show.usecase;

import com.ticket.core.api.controller.response.ShowOpeningSoonSummaryResponse;
import com.ticket.core.domain.show.ShowListQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetSaleStartApproachingShowsUseCase {
    private final ShowListQueryRepository showListQueryRepository;

    public GetSaleStartApproachingShowsUseCase(final ShowListQueryRepository showListQueryRepository) {
        this.showListQueryRepository = showListQueryRepository;
    }

    public record Input(String category, int size) {
    }

    public record Output(List<ShowOpeningSoonSummaryResponse> shows) {
    }

    public Output execute(final Input input) {
        return new Output(showListQueryRepository.findShowsSaleOpeningSoon(input.category, input.size));
    }
}
