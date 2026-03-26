package com.ticket.core.domain.show.query;

import com.ticket.core.domain.show.meta.Region;
import com.ticket.core.domain.show.meta.SaleType;
import com.ticket.core.domain.show.query.ShowListQueryRepository;
import com.ticket.core.domain.show.query.model.ShowParam;
import com.ticket.core.support.cursor.CursorSlice;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetShowsUseCase {
    private final ShowListQueryRepository showListQueryRepository;

    public record Input(ShowParam param, int size, ShowSort sort) {
    }

    public record ShowItem(
            Long id,
            String title,
            String subTitle,
            String image,
            List<String> genreNames,
            LocalDate startDate,
            LocalDate endDate,
            long viewCount,
            SaleType saleType,
            LocalDateTime saleStartDate,
            LocalDateTime saleEndDate,
            LocalDateTime createdAt,
            Region region,
            String venue
    ) {
    }

    public record Output(Slice<ShowItem> shows, String nextCursor) {
    }

    public Output execute(final Input input) {
        final CursorSlice<ShowItem> result = showListQueryRepository.findAllBySearch(
                input.param, input.size, input.sort.apiValue());
        return new Output(result.slice(), result.nextCursor());
    }
}
