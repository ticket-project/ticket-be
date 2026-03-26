package com.ticket.core.domain.show.query;

import com.ticket.core.domain.show.meta.Region;
import com.ticket.core.domain.show.query.ShowListQueryRepository;
import com.ticket.core.domain.show.query.model.ShowSearchCriteria;
import com.ticket.core.support.cursor.CursorSlice;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SearchShowsUseCase {
    private final ShowListQueryRepository showListQueryRepository;

    public record Input(ShowSearchCriteria request, int size, ShowSort sort) {
    }

    public record ShowSearchItem(
            Long id,
            String title,
            String image,
            String venue,
            LocalDate startDate,
            LocalDate endDate,
            Region region,
            long viewCount
    ) {
    }

    public record Output(Slice<ShowSearchItem> shows, String nextCursor) {
    }

    public Output execute(final Input input) {
        final CursorSlice<ShowSearchItem> result = showListQueryRepository.searchShows(
                input.request, input.size, input.sort.apiValue());
        return new Output(result.slice(), result.nextCursor());
    }
}
