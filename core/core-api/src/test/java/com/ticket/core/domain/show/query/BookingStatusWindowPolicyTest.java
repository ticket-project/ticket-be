package com.ticket.core.domain.show.query;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.ticket.core.enums.BookingStatus;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
class BookingStatusWindowPolicyTest {

    private final BookingStatusWindowPolicy bookingStatusWindowPolicy =
            new BookingStatusWindowPolicy(Clock.fixed(Instant.parse("2026-03-15T10:00:00Z"), ZoneId.of("Asia/Seoul")));

    @Test
    void bookingStatus가_null이면_null을_반환한다() {
        assertThat(bookingStatusWindowPolicy.condition(null)).isNull();
    }

    @Test
    void bookingStatus별로_고정시각_기준_조건식을_반환한다() {
        BooleanExpression beforeOpen = bookingStatusWindowPolicy.condition(BookingStatus.BEFORE_OPEN);
        BooleanExpression onSale = bookingStatusWindowPolicy.condition(BookingStatus.ON_SALE);
        BooleanExpression closed = bookingStatusWindowPolicy.condition(BookingStatus.CLOSED);

        assertThat(beforeOpen).isNotNull();
        assertThat(onSale).isNotNull();
        assertThat(closed).isNotNull();
        assertThat(beforeOpen.toString()).contains("2026-03-15T19:00");
        assertThat(onSale.toString()).contains("2026-03-15T19:00");
        assertThat(closed.toString()).contains("2026-03-15T19:00");
    }
}
