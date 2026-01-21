package com.ticket.core.domain.show;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticket.core.api.controller.request.ShowSearchParam;
import com.ticket.core.api.controller.response.ShowResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

@Repository
public class ShowQuerydslRepository {
    private final JPAQueryFactory queryFactory;

    public ShowQuerydslRepository(final JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public Slice<ShowResponse> findAllBySearch(final ShowSearchParam param, final Pageable pageable) {
        return null;
    }
}
