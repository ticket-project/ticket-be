package com.ticket.core.domain.show.usecase;

import com.ticket.core.api.controller.response.ShowOpeningSoonResponse;
import com.ticket.core.domain.show.ShowQuerydslRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetShowsOpeningSoonUseCase {
    private final ShowQuerydslRepository showQuerydslRepository;

    public GetShowsOpeningSoonUseCase(final ShowQuerydslRepository showQuerydslRepository) {
        this.showQuerydslRepository = showQuerydslRepository;
    }

    public record Input(String category, int size) {
    }

    public record Output(List<ShowOpeningSoonResponse> shows) {
    }

    public Output execute(final Input input) {
        return new Output(showQuerydslRepository.findShowsOpeningSoon(input.category, input.size));
    }
}
