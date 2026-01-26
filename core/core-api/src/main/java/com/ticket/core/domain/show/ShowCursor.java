package com.ticket.core.domain.show;

public record ShowCursor(
        ShowSortKey sort,
        String dir,
        String lastValue,
        Long lastId
) {}