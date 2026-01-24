package com.ticket.core.support.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.domain.Slice;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SliceResponse<T>(
        List<T> items,
        boolean hasNext,
        int size,
        int numberOfElements,
        String nextCursor
) {
    public static <T> SliceResponse<T> from(final Slice<T> slice, final String nextCursor) {
        return new SliceResponse<>(
                slice.getContent(),
                slice.hasNext(),
                slice.getSize(),
                slice.getNumberOfElements(),
                nextCursor
        );
    }
}
