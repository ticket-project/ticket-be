package com.ticket.core.domain.showwaitlist.query;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticket.core.support.cursor.CursorSlice;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.ticket.core.domain.show.model.QShow.show;
import static com.ticket.core.domain.show.venue.QVenue.venue;
import static com.ticket.core.domain.showwaitlist.model.QShowWaitlist.showWaitlist;

@Repository
@RequiredArgsConstructor
public class ShowWaitlistQueryRepository {

    private final JPAQueryFactory queryFactory;

    public CursorSlice<GetMyShowWaitlistUseCase.ShowWaitlistSummary> findMyWaitlistedShows(
            final Long memberId,
            final Long cursorWaitlistId,
            final int size
    ) {
        final BooleanBuilder where = new BooleanBuilder();
        where.and(showWaitlist.member.id.eq(memberId));

        if (cursorWaitlistId != null) {
            where.and(showWaitlist.id.lt(cursorWaitlistId));
        }

        final List<Tuple> rows = queryFactory
                .select(
                        showWaitlist.id,
                        show.id,
                        show.title,
                        show.image,
                        show.startDate,
                        show.endDate,
                        show.saleStartDate,
                        venue.name,
                        showWaitlist.createdAt
                )
                .from(showWaitlist)
                .join(showWaitlist.show, show)
                .leftJoin(show.venue, venue)
                .where(where)
                .orderBy(showWaitlist.id.desc())
                .limit(size + 1L)
                .fetch();

        if (rows.isEmpty()) {
            return emptyCursorSlice(size);
        }

        final boolean hasNext = rows.size() > size;
        final List<Tuple> pageRows = hasNext ? rows.subList(0, size) : rows;

        final List<GetMyShowWaitlistUseCase.ShowWaitlistSummary> items = pageRows.stream()
                .map(this::mapRow)
                .toList();

        final Slice<GetMyShowWaitlistUseCase.ShowWaitlistSummary> slice =
                new SliceImpl<>(items, PageRequest.of(0, size), hasNext);
        final String nextCursor = hasNext
                ? String.valueOf(pageRows.get(pageRows.size() - 1).get(showWaitlist.id))
                : null;
        return new CursorSlice<>(slice, nextCursor);
    }

    private <T> CursorSlice<T> emptyCursorSlice(final int size) {
        return new CursorSlice<>(new SliceImpl<>(List.of(), PageRequest.of(0, size), false), null);
    }

    private GetMyShowWaitlistUseCase.ShowWaitlistSummary mapRow(final Tuple tuple) {
        return new GetMyShowWaitlistUseCase.ShowWaitlistSummary(
                tuple.get(show.id),
                tuple.get(show.title),
                tuple.get(show.image),
                tuple.get(show.startDate),
                tuple.get(show.endDate),
                tuple.get(show.saleStartDate),
                tuple.get(venue.name),
                tuple.get(showWaitlist.createdAt)
        );
    }
}
