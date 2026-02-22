package com.ticket.core.domain.show.usecase;

import com.ticket.core.api.controller.request.ShowParam;
import com.ticket.core.api.controller.response.ShowResponse;
import com.ticket.core.domain.show.ShowListQueryRepository;
import com.ticket.core.support.cursor.CursorSlice;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetShowsUseCase {
    private final ShowListQueryRepository showListQueryRepository;

    public record Input(ShowParam param, int size, String sort) {
    }

    public record Output(Slice<ShowResponse> shows, String nextCursor) {
    }

    public Output execute(final Input input) {
        CursorSlice<ShowResponse> result = showListQueryRepository.findAllBySearch(
                input.param, input.size, input.sort);
        return new Output(result.slice(), result.nextCursor());
    }

}
