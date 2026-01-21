package com.ticket.core.domain.show.usecase;

import com.ticket.core.api.controller.request.ShowSearchParam;
import com.ticket.core.api.controller.response.ShowResponse;
import com.ticket.core.domain.show.ShowQuerydslRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
public class SearchShowsUseCase {
    private final ShowQuerydslRepository showQuerydslRepository;

    public SearchShowsUseCase(final ShowQuerydslRepository showQuerydslRepository) {
        this.showQuerydslRepository = showQuerydslRepository;
    }

    public record Input(ShowSearchParam param,
                        Pageable pageable) {
    }

    public record Output(ShowSearchParam param) {
    }

    public Output execute(Input input) {
        Slice<ShowResponse> responses = showQuerydslRepository.findAllBySearch(input.param, input.pageable);
        return null;
    }

}
