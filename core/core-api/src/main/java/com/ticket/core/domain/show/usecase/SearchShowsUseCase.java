package com.ticket.core.domain.show.usecase;

import com.ticket.core.api.controller.request.ShowSearchRequest;
import com.ticket.core.api.controller.response.ShowSearchResponse;
import com.ticket.core.domain.show.ShowListQueryRepository;
import com.ticket.core.support.cursor.CursorSlice;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 공연 검색 UseCase
 * - 검색어, 필터, 정렬 조건으로 공연 목록 조회
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SearchShowsUseCase {
    private final ShowListQueryRepository showListQueryRepository;

    public record Input(ShowSearchRequest request, int size, String sort) {
    }

    public record Output(Slice<ShowSearchResponse> shows, String nextCursor) {
    }

    public Output execute(final Input input) {
        CursorSlice<ShowSearchResponse> result = showListQueryRepository.searchShows(
                input.request, input.size, input.sort);
        return new Output(result.slice(), result.nextCursor());
    }
}
