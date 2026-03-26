package com.ticket.core.domain.show.query;

import com.ticket.core.domain.show.meta.Region;
import com.ticket.core.domain.show.query.ShowListQueryRepository;
import com.ticket.core.domain.show.query.model.SaleOpeningSoonSearchParam;
import com.ticket.core.support.cursor.CursorSlice;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetSaleStartApproachingShowsPageUseCase {

    private final ShowListQueryRepository showListQueryRepository;

    public record Input(SaleOpeningSoonSearchParam param, int size, String sort) {
    }

    public record ShowOpeningSoonDetail(
            Long id,
            String title,
            String subTitle,
            String image,
            String venue,
            Region region,
            LocalDate startDate,
            LocalDate endDate,
            LocalDateTime saleStartDate,
            LocalDateTime saleEndDate,
            long viewCount
    ) {
    }

    public record Output(Slice<ShowOpeningSoonDetail> shows, String nextCursor) {
    }

    public Output execute(final Input input) {
        final CursorSlice<ShowOpeningSoonDetail> result = showListQueryRepository.findSaleOpeningSoonPage(
                input.param(),
                input.size(),
                input.sort()
        );
        return new Output(result.slice(), result.nextCursor());
    }
}
