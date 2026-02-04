package com.ticket.core.domain.show.usecase;

import com.ticket.core.api.controller.response.ShowOpeningSoonSummaryResponse;
import com.ticket.core.domain.show.ShowQuerydslRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetSaleStartApproachingShowsUseCase {
    private final ShowQuerydslRepository showQuerydslRepository;

    public GetSaleStartApproachingShowsUseCase(final ShowQuerydslRepository showQuerydslRepository) {
        this.showQuerydslRepository = showQuerydslRepository;
    }

    public record Input(String category, int size) {
    }

    public record Output(List<ShowOpeningSoonSummaryResponse> shows) {
    }

    public Output execute(final Input input) {
        return new Output(showQuerydslRepository.findShowsSaleOpeningSoon(input.category, input.size));
    }
}
