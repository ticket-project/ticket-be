package com.ticket.core.domain.show.query.usecase;

import com.ticket.core.api.controller.response.ShowDetailResponse;
import com.ticket.core.domain.show.meta.SaleType;
import com.ticket.core.domain.show.query.ShowFinder;
import com.ticket.core.enums.BookingStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetShowDetailUseCaseTest {

    @Mock
    private ShowFinder showFinder;
    @InjectMocks
    private GetShowDetailUseCase useCase;

    @Test
    void 공연_상세를_그대로_반환한다() {
        ShowDetailResponse response = new ShowDetailResponse(
                1L, "공연", "부제", "info", LocalDate.now(), LocalDate.now().plusDays(1), 120, 100L, 10L,
                BookingStatus.ON_SALE, SaleType.GENERAL, LocalDateTime.now(), LocalDateTime.now().plusDays(1), "image",
                null, null, List.of("장르"), List.of(), List.of()
        );
        when(showFinder.findShowDetail(1L)).thenReturn(response);

        GetShowDetailUseCase.Output output = useCase.execute(new GetShowDetailUseCase.Input(1L));

        assertThat(output.show()).isEqualTo(response);
        verify(showFinder).findShowDetail(1L);
    }
}
