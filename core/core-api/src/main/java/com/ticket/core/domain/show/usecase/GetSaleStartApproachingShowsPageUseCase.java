package com.ticket.core.domain.show.usecase;

import com.ticket.core.api.controller.request.SaleOpeningSoonSearchParam;
import com.ticket.core.api.controller.response.ShowOpeningSoonDetailResponse;
import com.ticket.core.domain.show.ShowQuerydslRepository;
import com.ticket.core.support.cursor.CursorSlice;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 판매 오픈 예정 공연 무한스크롤 조회 UseCase
 */
@Service
@Transactional(readOnly = true)
public class GetSaleStartApproachingShowsPageUseCase {
    
    private final ShowQuerydslRepository showQuerydslRepository;

    public GetSaleStartApproachingShowsPageUseCase(ShowQuerydslRepository showQuerydslRepository) {
        this.showQuerydslRepository = showQuerydslRepository;
    }

    public record Input(SaleOpeningSoonSearchParam param, int size, String sort) {}
    
    public record Output(Slice<ShowOpeningSoonDetailResponse> shows, String nextCursor) {}

    public Output execute(Input input) {
        CursorSlice<ShowOpeningSoonDetailResponse> result = showQuerydslRepository.findSaleOpeningSoonPage(
                input.param(),
                input.size(),
                input.sort()
        );
        return new Output(result.slice(), result.nextCursor());
    }
}
