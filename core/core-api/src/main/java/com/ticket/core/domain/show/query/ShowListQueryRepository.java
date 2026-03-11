package com.ticket.core.domain.show.query;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticket.core.api.controller.response.ShowOpeningSoonDetailResponse;
import com.ticket.core.api.controller.response.ShowOpeningSoonSummaryResponse;
import com.ticket.core.api.controller.response.ShowResponse;
import com.ticket.core.api.controller.response.ShowSearchResponse;
import com.ticket.core.api.controller.response.ShowSummaryResponse;
import com.ticket.core.domain.show.Show;
import com.ticket.core.domain.show.ShowSortKey;
import com.ticket.core.domain.show.query.ShowSortSupport.SortOrder;
import com.ticket.core.domain.show.query.model.SaleOpeningSoonSearchParam;
import com.ticket.core.domain.show.query.model.ShowParam;
import com.ticket.core.domain.show.query.model.ShowSearchRequest;
import com.ticket.core.support.cursor.CursorSlice;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.ticket.core.domain.show.QCategory.category;
import static com.ticket.core.domain.show.QGenre.genre;
import static com.ticket.core.domain.show.QShow.show;
import static com.ticket.core.domain.show.QShowGenre.showGenre;
import static com.ticket.core.domain.show.QVenue.venue;

/**
 * Show 목록 조회 전용 Repository
 * - 메인 페이지 / 검색 / 오픈예정 목록 등 리스트 쿼리 담당
 */
@Repository
public class ShowListQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final ShowQueryHelper queryHelper;
    private final ShowSortSupport sortSupport;
    private final ShowCursorSupport cursorSupport;

    public ShowListQueryRepository(
            final JPAQueryFactory queryFactory,
            final ShowQueryHelper queryHelper,
            final ShowSortSupport sortSupport,
            final ShowCursorSupport cursorSupport
    ) {
        this.queryFactory = queryFactory;
        this.queryHelper = queryHelper;
        this.sortSupport = sortSupport;
        this.cursorSupport = cursorSupport;
    }

    // ========== 메인 페이지 API ==========

    public CursorSlice<ShowResponse> findAllBySearch(final ShowParam param, final int size, final String sort) {
        final SortOrder sortOrder = sortSupport.resolveSortOrder(sort);
        final BooleanBuilder where = buildMainListCondition(param, sortOrder);

        return findCursorPage(
                size,
                param.getCursor(),
                where,
                sortOrder,
                this::fetchShowPageRows,
                (context, ids) -> fetchMainShowResponses(ids, context.primaryOrder(), context.tieBreakerOrder())
        );
    }

    public List<ShowSummaryResponse> findLatestShows(final String categoryCode, final int limit) {
        return queryFactory
                .select(Projections.constructor(ShowSummaryResponse.class,
                        show.id, show.title, show.image, show.startDate, show.endDate, venue.name, show.createdAt))
                .distinct()
                .from(show)
                .leftJoin(show.venue, venue)
                .leftJoin(showGenre).on(showGenre.show.eq(show))
                .leftJoin(genre).on(showGenre.genre.eq(genre))
                .leftJoin(category).on(genre.category.eq(category))
                .where(queryHelper.categoryCodeEq(categoryCode))
                .orderBy(show.createdAt.desc())
                .limit(limit)
                .fetch();
    }

    public List<ShowOpeningSoonSummaryResponse> findShowsSaleOpeningSoon(final String categoryCode, final int limit) {
        return queryFactory
                .select(Projections.constructor(ShowOpeningSoonSummaryResponse.class,
                        show.id, show.title, show.image, venue.name, show.saleStartDate))
                .distinct()
                .from(show)
                .leftJoin(show.venue, venue)
                .leftJoin(showGenre).on(showGenre.show.eq(show))
                .leftJoin(genre).on(showGenre.genre.eq(genre))
                .leftJoin(category).on(genre.category.eq(category))
                .where(queryHelper.categoryCodeEq(categoryCode), queryHelper.saleStartDateGoe(LocalDateTime.now()))
                .orderBy(show.saleStartDate.asc())
                .limit(limit)
                .fetch();
    }

    public CursorSlice<ShowOpeningSoonDetailResponse> findSaleOpeningSoonPage(
            final SaleOpeningSoonSearchParam param,
            final int size,
            final String sort
    ) {
        final SortOrder sortOrder = sortSupport.resolveSortOrder(sort);
        final BooleanBuilder where = buildSaleOpeningCondition(param);

        return findCursorPage(
                size,
                param.getCursor(),
                where,
                sortOrder,
                this::fetchShowPageRows,
                (context, ids) -> fetchSaleOpeningResponses(ids, context.primaryOrder(), context.tieBreakerOrder())
        );
    }

    // ========== 검색 API ==========

    public CursorSlice<ShowSearchResponse> searchShows(
            final ShowSearchRequest request,
            final int size,
            final String sort
    ) {
        final SortOrder sortOrder = sortSupport.resolveSortOrder(sort);
        final BooleanBuilder where = buildSearchCondition(request, sortOrder);

        return findCursorPage(
                size,
                request.getCursor(),
                where,
                sortOrder,
                this::fetchShowPageRows,
                (context, ids) -> fetchSearchResponses(ids, context.primaryOrder(), context.tieBreakerOrder())
        );
    }

    public long countSearchShows(final ShowSearchRequest request) {
        final BooleanBuilder where = buildSearchCondition(request, null);
        final Long count = queryFactory
                .select(show.id.countDistinct())
                .from(show)
                .leftJoin(showGenre).on(showGenre.show.eq(show))
                .leftJoin(genre).on(showGenre.genre.eq(genre))
                .leftJoin(category).on(genre.category.eq(category))
                .where(where)
                .fetchOne();
        return count != null ? count : 0L;
    }

    // ========== 조건 조립 ==========

    private BooleanBuilder buildMainListCondition(final ShowParam param, final SortOrder sortOrder) {
        final BooleanBuilder where = new BooleanBuilder();
        where.and(queryHelper.categoryCodeEq(param.getCategory()));
        where.and(queryHelper.regionEq(param.getRegion()));
        where.and(queryHelper.genreEq(param.getGenre()));
        appendShowStartApproachingCondition(where, sortOrder);
        return where;
    }

    private BooleanBuilder buildSaleOpeningCondition(final SaleOpeningSoonSearchParam param) {
        final BooleanBuilder where = new BooleanBuilder();
        where.and(show.saleStartDate.goe(LocalDateTime.now()));
        where.and(queryHelper.categoryCodeEq(param.getCategory()));
        where.and(queryHelper.regionEq(param.getRegion()));
        where.and(queryHelper.titleContains(param.getTitle()));
        where.and(queryHelper.saleStartDateGoe(param.getSaleStartDateFrom()));
        where.and(queryHelper.saleStartDateLoe(param.getSaleStartDateTo()));
        where.and(queryHelper.saleEndDateGoe(param.getSaleEndDateFrom()));
        where.and(queryHelper.saleEndDateLoe(param.getSaleEndDateTo()));
        return where;
    }

    private BooleanBuilder buildSearchCondition(final ShowSearchRequest request, final SortOrder sortOrder) {
        final BooleanBuilder where = new BooleanBuilder();
        where.and(queryHelper.keywordContains(request.getKeyword()));
        where.and(queryHelper.categoryCodeEq(request.getCategory()));
        where.and(queryHelper.regionEq(request.getRegion()));
        where.and(queryHelper.startDateGoe(request.getStartDateFrom()));
        where.and(queryHelper.startDateLoe(request.getStartDateTo()));
        where.and(queryHelper.bookingStatusCondition(request.getBookingStatus()));
        appendShowStartApproachingCondition(where, sortOrder);
        return where;
    }

    private void appendShowStartApproachingCondition(final BooleanBuilder where, final SortOrder sortOrder) {
        if (sortOrder != null && ShowSortKey.SHOW_START_APPROACHING.equals(sortOrder.key())) {
            where.and(show.startDate.goe(LocalDate.now()));
        }
    }

    // ========== 공통 페이지 조회 ==========

    private <T> CursorSlice<T> findCursorPage(
            final int size,
            final String cursor,
            final BooleanBuilder where,
            final SortOrder sortOrder,
            final Function<QueryPageContext, List<Tuple>> rowFetcher,
            final BiFunction<QueryPageContext, List<Long>, List<T>> resultFetcher
    ) {
        cursorSupport.applyCursor(where, cursor, sortOrder);

        final QueryPageContext context = new QueryPageContext(
                size,
                where,
                sortOrder,
                sortSupport.primaryOrderSpecifier(sortOrder),
                sortSupport.tieBreakerOrder(sortOrder)
        );

        final List<Tuple> rows = rowFetcher.apply(context);
        final List<Long> ids = extractIds(rows);
        if (ids.isEmpty()) {
            return emptyCursorSlice(size);
        }

        List<T> results = new ArrayList<>(resultFetcher.apply(context, ids));
        final boolean hasNext = results.size() > size;
        if (hasNext) {
            results = results.subList(0, size);
        }

        final Slice<T> slice = new SliceImpl<>(results, PageRequest.of(0, size), hasNext);
        final String nextCursor = hasNext
                ? buildNextCursor(rows, size, sortOrder)
                : null;

        return new CursorSlice<>(slice, nextCursor);
    }

    private List<Tuple> fetchShowPageRows(final QueryPageContext context) {
        return queryFactory
                .select(show.id, show.startDate, show.createdAt, show.saleStartDate, show.viewCount)
                .distinct()
                .from(show)
                .leftJoin(showGenre).on(showGenre.show.eq(show))
                .leftJoin(genre).on(showGenre.genre.eq(genre))
                .leftJoin(category).on(genre.category.eq(category))
                .where(context.where())
                .orderBy(context.primaryOrder(), context.tieBreakerOrder())
                .limit(context.size() + 1L)
                .fetch();
    }

    private List<ShowResponse> fetchMainShowResponses(
            final List<Long> ids,
            final OrderSpecifier<?> primaryOrder,
            final OrderSpecifier<Long> tieBreakerOrder
    ) {
        final Map<Long, List<String>> genreMap = fetchGenreMap(ids);
        final List<Show> shows = queryFactory
                .selectFrom(show)
                .leftJoin(show.venue, venue).fetchJoin()
                .where(show.id.in(ids))
                .orderBy(primaryOrder, tieBreakerOrder)
                .fetch();

        return new ArrayList<>(shows.stream()
                .map(s -> new ShowResponse(
                        s.getId(), s.getTitle(), s.getSubTitle(), s.getImage(),
                        genreMap.getOrDefault(s.getId(), new ArrayList<>()),
                        s.getStartDate(), s.getEndDate(), s.getViewCount(),
                        s.getSaleType(), s.getSaleStartDate(), s.getSaleEndDate(),
                        s.getCreatedAt(),
                        s.getVenue() != null ? s.getVenue().getRegion() : null,
                        s.getVenue() != null ? s.getVenue().getName() : null))
                .toList());
    }

    private List<ShowOpeningSoonDetailResponse> fetchSaleOpeningResponses(
            final List<Long> ids,
            final OrderSpecifier<?> primaryOrder,
            final OrderSpecifier<Long> tieBreakerOrder
    ) {
        return queryFactory
                .select(Projections.constructor(ShowOpeningSoonDetailResponse.class,
                        show.id, show.title, show.subTitle, show.image, venue.name, venue.region,
                        show.startDate, show.endDate, show.saleStartDate, show.saleEndDate, show.viewCount))
                .from(show)
                .leftJoin(show.venue, venue)
                .where(show.id.in(ids))
                .orderBy(primaryOrder, tieBreakerOrder)
                .fetch();
    }

    private List<ShowSearchResponse> fetchSearchResponses(
            final List<Long> ids,
            final OrderSpecifier<?> primaryOrder,
            final OrderSpecifier<Long> tieBreakerOrder
    ) {
        return queryFactory
                .select(Projections.constructor(ShowSearchResponse.class,
                        show.id, show.title, show.image, venue.name,
                        show.startDate, show.endDate, venue.region, show.viewCount))
                .from(show)
                .leftJoin(show.venue, venue)
                .where(show.id.in(ids))
                .orderBy(primaryOrder, tieBreakerOrder)
                .fetch();
    }

    // ========== 내부 헬퍼 ==========

    private List<Long> extractIds(final List<Tuple> rows) {
        return rows.stream().map(t -> t.get(show.id)).toList();
    }

    private <T> CursorSlice<T> emptyCursorSlice(final int size) {
        return new CursorSlice<>(new SliceImpl<>(List.of(), PageRequest.of(0, size), false), null);
    }

    private String buildNextCursor(final List<Tuple> rows, final int size, final SortOrder sortOrder) {
        final Tuple lastRow = rows.get(size - 1);
        final Long lastId = lastRow.get(show.id);
        final String lastValue = resolveLastValue(lastRow, sortOrder);
        return cursorSupport.buildNextCursor(lastId, sortOrder.key(), sortOrder.direction(), lastValue);
    }

    private String resolveLastValue(final Tuple lastRow, final SortOrder sortOrder) {
        return switch (sortOrder.key()) {
            case POPULAR -> String.valueOf(lastRow.get(show.viewCount));
            case LATEST -> lastRow.get(show.createdAt).toString();
            case SHOW_START_APPROACHING -> lastRow.get(show.startDate).toString();
            case SALE_START_APPROACHING -> lastRow.get(show.saleStartDate).toString();
        };
    }

    private Map<Long, List<String>> fetchGenreMap(final List<Long> ids) {
        final List<Tuple> genreTuples = queryFactory
                .select(show.id, genre.name)
                .from(show)
                .leftJoin(showGenre).on(showGenre.show.eq(show))
                .leftJoin(genre).on(showGenre.genre.eq(genre))
                .where(show.id.in(ids))
                .fetch();

        final Map<Long, List<String>> genreMap = new LinkedHashMap<>();
        for (Tuple tuple : genreTuples) {
            final Long showId = tuple.get(show.id);
            final String genreName = tuple.get(genre.name);
            if (genreName != null) {
                genreMap.computeIfAbsent(showId, key -> new ArrayList<>()).add(genreName);
            }
        }
        return genreMap;
    }

    private record QueryPageContext(
            int size,
            BooleanBuilder where,
            SortOrder sortOrder,
            OrderSpecifier<?> primaryOrder,
            OrderSpecifier<Long> tieBreakerOrder
    ) {
    }
}
