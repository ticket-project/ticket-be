package com.ticket.core.domain.show.usecase;

import com.ticket.core.api.controller.response.ShowDetailResponse;
import com.ticket.core.domain.show.ShowFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetShowDetailUseCase {

    private final ShowFinder showFinder;

    public record Input(Long showId) {}

    public record Output(ShowDetailResponse show) {}

    public Output execute(Input input) {
        ShowDetailResponse detail = showFinder.findShowDetail(input.showId());
        return new Output(detail);
    }
}
