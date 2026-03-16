package com.ticket.core.domain.show.query;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.ticket.core.enums.BookingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

import static com.ticket.core.domain.show.QShow.show;

@Component
@RequiredArgsConstructor
public class BookingStatusWindowPolicy {

    private final Clock clock;

    public BooleanExpression condition(final BookingStatus bookingStatus) {
        if (bookingStatus == null) {
            return null;
        }

        final LocalDateTime now = LocalDateTime.now(clock);
        return switch (bookingStatus) {
            case BEFORE_OPEN -> show.saleStartDate.gt(now);
            case ON_SALE -> show.saleStartDate.loe(now).and(show.saleEndDate.goe(now));
            case CLOSED -> show.saleEndDate.lt(now);
        };
    }
}
