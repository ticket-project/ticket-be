package com.ticket.core.domain.performeralert.query;

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

import static com.ticket.core.domain.performeralert.model.QPerformerAlert.performerAlert;
import static com.ticket.core.domain.show.performer.QPerformer.performer;

@Repository
@RequiredArgsConstructor
public class PerformerAlertQueryRepository {

    private final JPAQueryFactory queryFactory;

    public CursorSlice<GetMyPerformerAlertsUseCase.PerformerAlertSummary> findMyPerformerAlerts(
            final Long memberId,
            final Long cursorAlertId,
            final int size
    ) {
        final BooleanBuilder where = new BooleanBuilder();
        where.and(performerAlert.member.id.eq(memberId));

        if (cursorAlertId != null) {
            where.and(performerAlert.id.lt(cursorAlertId));
        }

        final List<Tuple> rows = queryFactory
                .select(
                        performerAlert.id,
                        performer.id,
                        performer.name,
                        performer.profileImageUrl,
                        performerAlert.createdAt
                )
                .from(performerAlert)
                .join(performerAlert.performer, performer)
                .where(where)
                .orderBy(performerAlert.id.desc())
                .limit(size + 1L)
                .fetch();

        if (rows.isEmpty()) {
            return emptyCursorSlice(size);
        }

        final boolean hasNext = rows.size() > size;
        final List<Tuple> pageRows = hasNext ? rows.subList(0, size) : rows;

        final List<GetMyPerformerAlertsUseCase.PerformerAlertSummary> items = pageRows.stream()
                .map(this::mapRow)
                .toList();

        final Slice<GetMyPerformerAlertsUseCase.PerformerAlertSummary> slice =
                new SliceImpl<>(items, PageRequest.of(0, size), hasNext);
        final String nextCursor = hasNext
                ? String.valueOf(pageRows.get(pageRows.size() - 1).get(performerAlert.id))
                : null;
        return new CursorSlice<>(slice, nextCursor);
    }

    private <T> CursorSlice<T> emptyCursorSlice(final int size) {
        return new CursorSlice<>(new SliceImpl<>(List.of(), PageRequest.of(0, size), false), null);
    }

    private GetMyPerformerAlertsUseCase.PerformerAlertSummary mapRow(final Tuple tuple) {
        return new GetMyPerformerAlertsUseCase.PerformerAlertSummary(
                tuple.get(performer.id),
                tuple.get(performer.name),
                tuple.get(performer.profileImageUrl),
                tuple.get(performerAlert.createdAt)
        );
    }
}
