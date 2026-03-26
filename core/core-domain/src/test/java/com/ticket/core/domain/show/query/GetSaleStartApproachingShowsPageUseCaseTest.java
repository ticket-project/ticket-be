package com.ticket.core.domain.show.query;

import com.ticket.core.domain.show.meta.Region;
import com.ticket.core.domain.show.query.ShowListQueryRepository;
import com.ticket.core.domain.show.query.model.SaleOpeningSoonSearchParam;
import com.ticket.core.support.cursor.CursorSlice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.SliceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
class GetSaleStartApproachingShowsPageUseCaseTest {

    @Mock
    private ShowListQueryRepository showListQueryRepository;

    @InjectMocks
    private GetSaleStartApproachingShowsPageUseCase useCase;

    @Test
    void 커서_페이지_응답을_그대로_전달한다() {
        SaleOpeningSoonSearchParam param = new SaleOpeningSoonSearchParam(null, null, null, null, null, null, null, null);
        GetSaleStartApproachingShowsPageUseCase.ShowOpeningSoonDetail show =
                new GetSaleStartApproachingShowsPageUseCase.ShowOpeningSoonDetail(
                        1L,
                        "공연",
                        "부제",
                        "image",
                        "장소",
                        Region.SEOUL,
                        LocalDate.now(),
                        LocalDate.now().plusDays(1),
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(1),
                        100L
                );
        CursorSlice<GetSaleStartApproachingShowsPageUseCase.ShowOpeningSoonDetail> result =
                new CursorSlice<>(new SliceImpl<>(List.of(show)), "next-cursor");
        when(showListQueryRepository.findSaleOpeningSoonPage(param, 10, "popular")).thenReturn(result);

        GetSaleStartApproachingShowsPageUseCase.Output output =
                useCase.execute(new GetSaleStartApproachingShowsPageUseCase.Input(param, 10, "popular"));

        assertThat(output.shows().getContent()).containsExactly(show);
        assertThat(output.nextCursor()).isEqualTo("next-cursor");
        verify(showListQueryRepository).findSaleOpeningSoonPage(param, 10, "popular");
    }

    @Test
    void 판매_오픈_예정_공연이_없으면_빈_슬라이스와_null_커서를_반환한다() {
        SaleOpeningSoonSearchParam param = new SaleOpeningSoonSearchParam(null, null, null, null, null, null, null, null);
        CursorSlice<GetSaleStartApproachingShowsPageUseCase.ShowOpeningSoonDetail> result =
                new CursorSlice<>(new SliceImpl<>(List.of()), null);
        when(showListQueryRepository.findSaleOpeningSoonPage(param, 10, "popular")).thenReturn(result);

        GetSaleStartApproachingShowsPageUseCase.Output output =
                useCase.execute(new GetSaleStartApproachingShowsPageUseCase.Input(param, 10, "popular"));

        assertThat(output.shows().getContent()).isEmpty();
        assertThat(output.nextCursor()).isNull();
        verify(showListQueryRepository).findSaleOpeningSoonPage(param, 10, "popular");
    }
}
