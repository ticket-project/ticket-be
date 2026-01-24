package com.ticket.core.support.cursor;

import org.springframework.data.domain.Slice;

public record CursorSlice<T>(
        Slice<T> slice,
        String nextCursor
) {}