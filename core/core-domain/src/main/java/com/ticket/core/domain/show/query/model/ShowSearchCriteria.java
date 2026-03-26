package com.ticket.core.domain.show.query.model;

import com.ticket.core.domain.show.BookingStatus;
import com.ticket.core.domain.show.meta.Region;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ShowSearchCriteria {
    private String keyword;
    private String category;
    private BookingStatus bookingStatus;
    private LocalDate startDateFrom;
    private LocalDate startDateTo;
    private Region region;
    private String cursor;
}
