package com.ticket.core.domain.showlike;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticket.core.api.controller.response.ShowLikeSummaryResponse;
import com.ticket.core.support.cursor.CursorSlice;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.ticket.core.domain.show.QShow.show;
import static com.ticket.core.domain.show.QVenue.venue;
import static com.ticket.core.domain.showlike.QShowLike.showLike;

@Repository
public class ShowLikeQueryRepository {

    private final JPAQueryFactory queryFactory;

    public ShowLikeQueryRepository(final JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public CursorSlice<ShowLikeSummaryResponse> findMyLikedShows(
            final Long memberId,
            final Long cursorLikeId,
            final int size
    ) {
        final BooleanBuilder where = new BooleanBuilder();
        where.and(showLike.member.id.eq(memberId));

        if (cursorLikeId != null) {
            where.and(showLike.id.lt(cursorLikeId));
        }

        List<Tuple> rows = queryFactory
                .select(
                        showLike.id,
                        show.id,
                        show.title,
                        show.image,
                        show.startDate,
                        show.endDate,
                        venue.name,
                        showLike.createdAt
                )
                .from(showLike)
                .join(showLike.show, show)
                .leftJoin(show.venue, venue)
                .where(where)
                .orderBy(showLike.id.desc())
                .limit(size + 1L)
                .fetch();

        if (rows.isEmpty()) {
            return new CursorSlice<>(new SliceImpl<>(List.of(), PageRequest.of(0, size), false), null);
        }

        final boolean hasNext = rows.size() > size;
        if (hasNext) {
            rows = rows.subList(0, size);
        }

        final List<ShowLikeSummaryResponse> items = rows.stream()
                .map(this::mapRow)
                .toList();

        final Slice<ShowLikeSummaryResponse> slice = new SliceImpl<>(items, PageRequest.of(0, size), hasNext);
        final String nextCursor = hasNext ? String.valueOf(rows.get(rows.size() - 1).get(showLike.id)) : null;
        return new CursorSlice<>(slice, nextCursor);
    }

    private ShowLikeSummaryResponse mapRow(final Tuple tuple) {
        return new ShowLikeSummaryResponse(
                tuple.get(show.id),
                tuple.get(show.title),
                tuple.get(show.image),
                tuple.get(show.startDate),
                tuple.get(show.endDate),
                tuple.get(venue.name),
                tuple.get(showLike.createdAt)
        );
    }
}
