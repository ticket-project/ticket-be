package com.ticket.core.domain.show.query.model;

import com.ticket.core.domain.show.meta.ShowSortKey;

public record ShowCursor(
        ShowSortKey sort,
        String dir,
        String lastValue,
        Long lastId
) {}
