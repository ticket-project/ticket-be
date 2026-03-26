package com.ticket.core.api.controller.request;

import com.ticket.core.domain.show.BookingStatus;
import com.ticket.core.domain.show.meta.Region;
import com.ticket.core.domain.show.query.model.ShowSearchCriteria;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
class ShowSearchRequestTest {

    @Test
    void 요청값을_domain_검색조건으로_변환한다() {
        ShowSearchRequest request = new ShowSearchRequest(
                "뮤지컬",
                "MUSICAL",
                BookingStatus.ON_SALE,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30),
                Region.SEOUL,
                "cursor-1"
        );

        ShowSearchCriteria criteria = request.toCriteria();

        assertThat(criteria.getKeyword()).isEqualTo("뮤지컬");
        assertThat(criteria.getCategory()).isEqualTo("MUSICAL");
        assertThat(criteria.getBookingStatus()).isEqualTo(BookingStatus.ON_SALE);
        assertThat(criteria.getStartDateFrom()).isEqualTo(LocalDate.of(2026, 4, 1));
        assertThat(criteria.getStartDateTo()).isEqualTo(LocalDate.of(2026, 4, 30));
        assertThat(criteria.getRegion()).isEqualTo(Region.SEOUL);
        assertThat(criteria.getCursor()).isEqualTo("cursor-1");
    }
}
