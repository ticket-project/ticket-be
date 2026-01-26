package com.ticket.core.domain.show.usecase;

import com.ticket.core.api.controller.request.ShowSearchParam;
import com.ticket.core.api.controller.response.ShowResponse;
import com.ticket.core.domain.show.ShowQuerydslRepository;
import com.ticket.core.support.cursor.CursorSlice;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SearchShowsUseCase {
    private final ShowQuerydslRepository showQuerydslRepository;

    public SearchShowsUseCase(final ShowQuerydslRepository showQuerydslRepository) {
        this.showQuerydslRepository = showQuerydslRepository;
    }

    public record Input(ShowSearchParam param, int size, String sort) {
    }

    public record Output(Slice<ShowResponse> shows, String nextCursor) {
    }

    public Output execute(final Input input) {
        CursorSlice<ShowResponse> result = showQuerydslRepository.findAllBySearch(
                input.param, input.size, input.sort);
        return new Output(result.slice(), result.nextCursor());
    }

}

