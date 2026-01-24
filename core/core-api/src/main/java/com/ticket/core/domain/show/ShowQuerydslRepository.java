package com.ticket.core.domain.show;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticket.core.api.controller.request.ShowSearchParam;
import com.ticket.core.api.controller.response.ShowResponse;
import com.ticket.core.support.cursor.CursorCodec;
import com.ticket.core.support.cursor.CursorSlice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;

import static com.ticket.core.domain.show.QCategory.category;
import static com.ticket.core.domain.show.QShow.show;
import static com.ticket.core.domain.show.QShowCategory.showCategory;
import static com.ticket.core.domain.show.ShowSortKey.POPULAR;

@Repository
public class ShowQuerydslRepository {
    private final JPAQueryFactory queryFactory;
    private final CursorCodec cursorCodec;

    public ShowQuerydslRepository(final JPAQueryFactory queryFactory, final CursorCodec cursorCodec) {
        this.queryFactory = queryFactory;
        this.cursorCodec = cursorCodec;
    }

    public CursorSlice<ShowResponse> findAllBySearch(ShowSearchParam param, Pageable pageable) {
        int size = pageable.getPageSize();

        BooleanBuilder where = new BooleanBuilder();
        where.and(categoryNameEq(param.getCategory()));
        where.and(placeEq(param.getPlace()));

        SortOrder sortOrder = resolveSortOrder(pageable);

        OrderSpecifier<?> primaryOrder = primaryOrderSpecifier(sortOrder);
        OrderSpecifier<Long> tieBreakerOrder = (sortOrder.direction.isAscending() ? show.id.asc() : show.id.desc());

        if (StringUtils.hasText(param.getCursor())) {
            ShowCursor cursor = cursorCodec.decode(param.getCursor());
            validateCursorMatchesRequest(cursor, sortOrder);
            where.and(cursorCondition(cursor, sortOrder));
        }

        List<ShowResponse> results = queryFactory
                .select(Projections.constructor(ShowResponse.class,
                        show.id,
                        show.title,
                        show.subTitle,
                        show.startDate,
                        show.endDate,
                        show.viewCount,
                        show.place))
                .from(show)
                .leftJoin(showCategory).on(showCategory.show.eq(show))
                .leftJoin(category).on(showCategory.category.eq(category))
                .where(where)
                .limit(size + 1L)
                .orderBy(primaryOrder, tieBreakerOrder)
                .fetch();

        boolean hasNext = results.size() > size;
        if (hasNext) results = results.subList(0, size);

        Slice<ShowResponse> slice = new SliceImpl<>(results, pageable, hasNext);

        String nextCursor = null;
        if (hasNext && !results.isEmpty()) {
            ShowResponse last = results.getLast();
            ShowCursor next = buildNextCursor(last, sortOrder);
            nextCursor = cursorCodec.encode(next);
        }

        return new CursorSlice<>(slice, nextCursor);
    }

    private ShowCursor buildNextCursor(ShowResponse last, SortOrder sortOrder) {
        String lastValue = switch (sortOrder.key) {
            case POPULAR -> last.viewCount().toString();
            case LATEST -> last.startDate().toString();
            //todo 마감임박순은 무슨 조건으로 정렬?
            case ENDING_SOON -> last.title();
        };

        return new ShowCursor(
                sortOrder.key,
                sortOrder.direction.name(),
                lastValue,
                last.id()
        );
    }

    private BooleanExpression cursorCondition(ShowCursor cursor, SortOrder sortOrder) {
        boolean desc = sortOrder.direction().isDescending();
        Long lastId = cursor.lastId();

        return switch (sortOrder.key()) {

            case POPULAR -> {
                long last = Long.parseLong(cursor.lastValue());
                yield  show.viewCount.lt(last).or(show.viewCount.eq(last).and(show.id.lt(lastId)));
            }
            case LATEST -> {
                var last = LocalDate.parse(cursor.lastValue());
                yield desc
                        ? show.startDate.lt(last).or(show.startDate.eq(last).and(show.id.lt(lastId)))
                        : show.startDate.gt(last).or(show.startDate.eq(last).and(show.id.gt(lastId)));
            }
            case ENDING_SOON -> {
                LocalDate last = LocalDate.parse(cursor.lastValue());
                yield desc
                        ? show.endDate.lt(last).or(show.endDate.eq(last).and(show.id.lt(lastId)))
                        : show.endDate.gt(last).or(show.endDate.eq(last).and(show.id.gt(lastId)));
            }
        };
    }

    private void validateCursorMatchesRequest(ShowCursor cursor, SortOrder sortOrder) {
        if (!sortOrder.key.equals(cursor.sort())) {
            throw new IllegalArgumentException("cursor.sort와 요청 sort가 일치하지 않습니다.");
        }
        if (!sortOrder.direction.name().equalsIgnoreCase(cursor.dir())) {
            throw new IllegalArgumentException("cursor.dir와 요청 dir가 일치하지 않습니다.");
        }
        if (cursor.lastId() == null) {
            throw new IllegalArgumentException("cursor.lastId가 없습니다.");
        }

        // lastValue 필수
        if (!StringUtils.hasText(cursor.lastValue())) {
            throw new IllegalArgumentException("cursor.lastValue가 없습니다.");
        }
    }

    private OrderSpecifier<?> primaryOrderSpecifier(SortOrder sortOrder) {
        return switch (sortOrder.key()) {
            case POPULAR -> show.viewCount.desc();
            case LATEST -> show.createdAt.desc();
            case ENDING_SOON -> show.endDate.asc();
        };
    }

    private SortOrder resolveSortOrder(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return new SortOrder(POPULAR, Sort.Direction.ASC);
        }

        Iterator<Sort.Order> it = pageable.getSort().iterator();
        Sort.Order first = it.next();
        return new SortOrder(ShowSortKey.fromApiValue(first.getProperty()), first.getDirection());
    }

    private BooleanExpression categoryNameEq(String categoryName) {
        return StringUtils.hasText(categoryName)
                ? category.name.eq(categoryName)
                : null;
    }

    private BooleanExpression placeEq(String place) {
        return StringUtils.hasText(place)
                ? show.place.eq(place)
                : null;
    }

    private record SortOrder(ShowSortKey key, Sort.Direction direction) {
    }

}
