package com.ticket.core.api.controller.response;

import java.time.LocalDate;

public record ShowResponse(
        Long id,
        String title,
        String subTitle,
        LocalDate startDate,
        LocalDate endDate,
        String place) {
}
