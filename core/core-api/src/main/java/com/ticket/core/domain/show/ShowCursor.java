package com.ticket.core.domain.show;

public record ShowCursor(
        String sort,
        String dir,
        String lastValue,
        Long lastId
) {}