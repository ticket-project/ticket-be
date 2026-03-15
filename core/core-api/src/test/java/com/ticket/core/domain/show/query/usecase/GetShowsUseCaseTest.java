package com.ticket.core.domain.show.query.usecase;

import com.ticket.core.api.controller.response.ShowResponse;
import com.ticket.core.domain.show.meta.SaleType;
import com.ticket.core.domain.show.query.ShowListQueryRepository;
import com.ticket.core.domain.show.query.model.ShowParam;
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
class GetShowsUseCaseTest {

    @Mock
    private ShowListQueryRepository showListQueryRepository;
    @InjectMocks
    private GetShowsUseCase useCase;

    @Test
    void 공연_목록과_커서를_반환한다() {
        ShowParam param = new ShowParam(null, null, null, null);
        ShowResponse show = new ShowResponse(
                1L,
                "공연",
                "부제",
                "image",
                List.of("장르"),
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                10L,
                SaleType.GENERAL,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now(),
                null,
                "장소"
        );
        CursorSlice<ShowResponse> result = new CursorSlice<>(new SliceImpl<>(List.of(show)), "next");
        when(showListQueryRepository.findAllBySearch(param, 10, "popular")).thenReturn(result);

        GetShowsUseCase.Output output = useCase.execute(new GetShowsUseCase.Input(param, 10, "popular"));

        assertThat(output.shows().getContent()).containsExactly(show);
        assertThat(output.nextCursor()).isEqualTo("next");
        verify(showListQueryRepository).findAllBySearch(param, 10, "popular");
    }

    @Test
    void 공연이_없으면_빈_슬라이스와_null_커서를_반환한다() {
        ShowParam param = new ShowParam(null, null, null, null);
        CursorSlice<ShowResponse> result = new CursorSlice<>(new SliceImpl<>(List.of()), null);
        when(showListQueryRepository.findAllBySearch(param, 10, "popular")).thenReturn(result);

        GetShowsUseCase.Output output = useCase.execute(new GetShowsUseCase.Input(param, 10, "popular"));

        assertThat(output.shows().getContent()).isEmpty();
        assertThat(output.nextCursor()).isNull();
        verify(showListQueryRepository).findAllBySearch(param, 10, "popular");
    }
}
