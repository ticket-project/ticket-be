package com.ticket.core.domain.show.query;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticket.core.api.controller.response.ShowOpeningSoonDetailResponse;
import com.ticket.core.api.controller.response.ShowOpeningSoonSummaryResponse;
import com.ticket.core.api.controller.response.ShowResponse;
import com.ticket.core.api.controller.response.ShowSearchResponse;
import com.ticket.core.api.controller.response.ShowSummaryResponse;
import com.ticket.core.domain.show.Show;
import com.ticket.core.domain.show.image.ShowCardImagePathConverter;
import com.ticket.core.domain.show.query.ShowSortSupport.SortOrder;
import com.ticket.core.domain.show.query.model.SaleOpeningSoonSearchParam;
import com.ticket.core.domain.show.query.model.ShowParam;
import com.ticket.core.domain.show.query.model.ShowSearchRequest;
import com.ticket.core.support.cursor.CursorSlice;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.ticket.core.domain.show.category.QCategory.category;
import static com.ticket.core.domain.show.genre.QGenre.genre;
import static com.ticket.core.domain.show.QShow.show;
import static com.ticket.core.domain.show.mapping.QShowGenre.showGenre;
import static com.ticket.core.domain.show.venue.QVenue.venue;

/**
 * Show 목록 조회 전용 Repository
 * - 메인 페이지 / 검색 / 오픈예정 목록 등 리스트 쿼리 담당
 */
@Repository
@RequiredArgsConstructor
public class ShowListQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final ShowQueryHelper queryHelper;
    private final ShowConditionFactory showConditionFactory;
    private final ShowSortSupport sortSupport;
    private final ShowCursorPolicy showCursorPolicy;
    private final ShowCardImagePathConverter showCardImagePathConverter;

    // ========== 메인 페이지 API ==========

    public CursorSlice<ShowResponse> findAllBySearch(final ShowParam param, final int size, final String sort) {
        final SortOrder sortOrder = sortSupport.resolveSortOrder(sort);
        final BooleanBuilder where = showConditionFactory.buildMainListCondition(param, sortOrder);

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
                .select(show.id, show.title, show.image, show.startDate, show.endDate, venue.name, show.createdAt)
                .distinct()
                .from(show)
                .leftJoin(show.venue, venue)
                .leftJoin(showGenre).on(showGenre.show.eq(show))
                .leftJoin(genre).on(showGenre.genre.eq(genre))
                .leftJoin(category).on(genre.category.eq(category))
                .where(queryHelper.categoryCodeEq(categoryCode))
                .orderBy(show.createdAt.desc())
                .limit(limit)
                .fetch()
                .stream()
                .map(this::toShowSummaryResponse)
                .toList();
    }

    public List<ShowOpeningSoonSummaryResponse> findShowsSaleOpeningSoon(final String categoryCode, final int limit) {
        return queryFactory
                .select(show.id, show.title, show.image, venue.name, show.saleStartDate)
                .distinct()
                .from(show)
                .leftJoin(show.venue, venue)
                .leftJoin(showGenre).on(showGenre.show.eq(show))
                .leftJoin(genre).on(showGenre.genre.eq(genre))
                .leftJoin(category).on(genre.category.eq(category))
                .where(showConditionFactory.buildSaleOpeningSoonSummaryCondition(categoryCode))
                .orderBy(show.saleStartDate.asc())
                .limit(limit)
                .fetch()
                .stream()
                .map(this::toShowOpeningSoonSummaryResponse)
                .toList();
    }

    public CursorSlice<ShowOpeningSoonDetailResponse> findSaleOpeningSoonPage(
            final SaleOpeningSoonSearchParam param,
            final int size,
            final String sort
    ) {
        final SortOrder sortOrder = sortSupport.resolveSortOrder(sort);
        final BooleanBuilder where = showConditionFactory.buildSaleOpeningCondition(param);

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
        final BooleanBuilder where = showConditionFactory.buildSearchCondition(request, sortOrder);

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
        final BooleanBuilder where = showConditionFactory.buildSearchCondition(request, null);
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

    // ========== 공통 페이지 조회 ==========

    private <T> CursorSlice<T> findCursorPage(
            final int size,
            final String cursor,
            final BooleanBuilder where,
            final SortOrder sortOrder,
            final Function<QueryPageContext, List<Tuple>> rowFetcher,
            final BiFunction<QueryPageContext, List<Long>, List<T>> resultFetcher
    ) {
        showCursorPolicy.applyCursor(where, cursor, sortOrder);

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
                ? showCursorPolicy.buildNextCursor(rows, size, sortOrder)
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
                s.getId(), s.getTitle(), s.getSubTitle(), showCardImagePathConverter.toCardImage(s.getImage()),
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
                .select(show.id, show.title, show.subTitle, show.image, venue.name, venue.region,
                        show.startDate, show.endDate, show.saleStartDate, show.saleEndDate, show.viewCount)
                .from(show)
                .leftJoin(show.venue, venue)
                .where(show.id.in(ids))
                .orderBy(primaryOrder, tieBreakerOrder)
                .fetch()
                .stream()
                .map(this::toShowOpeningSoonDetailResponse)
                .toList();
    }

    private List<ShowSearchResponse> fetchSearchResponses(
            final List<Long> ids,
            final OrderSpecifier<?> primaryOrder,
            final OrderSpecifier<Long> tieBreakerOrder
    ) {
        return queryFactory
                .select(show.id, show.title, show.image, venue.name,
                        show.startDate, show.endDate, venue.region, show.viewCount)
                .from(show)
                .leftJoin(show.venue, venue)
                .where(show.id.in(ids))
                .orderBy(primaryOrder, tieBreakerOrder)
                .fetch()
                .stream()
                .map(this::toShowSearchResponse)
                .toList();
    }

    // ========== 내부 헬퍼 ==========

    private List<Long> extractIds(final List<Tuple> rows) {
        return rows.stream().map(t -> t.get(show.id)).toList();
    }

    private <T> CursorSlice<T> emptyCursorSlice(final int size) {
        return new CursorSlice<>(new SliceImpl<>(List.of(), PageRequest.of(0, size), false), null);
    }

    private ShowSummaryResponse toShowSummaryResponse(final Tuple tuple) {
        return new ShowSummaryResponse(
                tuple.get(show.id),
                tuple.get(show.title),
                showCardImagePathConverter.toCardImage(tuple.get(show.image)),
                tuple.get(show.startDate),
                tuple.get(show.endDate),
                tuple.get(venue.name),
                tuple.get(show.createdAt)
        );
    }

    private ShowOpeningSoonSummaryResponse toShowOpeningSoonSummaryResponse(final Tuple tuple) {
        return new ShowOpeningSoonSummaryResponse(
                tuple.get(show.id),
                tuple.get(show.title),
                showCardImagePathConverter.toCardImage(tuple.get(show.image)),
                tuple.get(venue.name),
                tuple.get(show.saleStartDate)
        );
    }

    private ShowOpeningSoonDetailResponse toShowOpeningSoonDetailResponse(final Tuple tuple) {
        return new ShowOpeningSoonDetailResponse(
                tuple.get(show.id),
                tuple.get(show.title),
                tuple.get(show.subTitle),
                showCardImagePathConverter.toCardImage(tuple.get(show.image)),
                tuple.get(venue.name),
                tuple.get(venue.region),
                tuple.get(show.startDate),
                tuple.get(show.endDate),
                tuple.get(show.saleStartDate),
                tuple.get(show.saleEndDate),
                tuple.get(show.viewCount)
        );
    }

    private ShowSearchResponse toShowSearchResponse(final Tuple tuple) {
        return new ShowSearchResponse(
                tuple.get(show.id),
                tuple.get(show.title),
                showCardImagePathConverter.toCardImage(tuple.get(show.image)),
                tuple.get(venue.name),
                tuple.get(show.startDate),
                tuple.get(show.endDate),
                tuple.get(venue.region),
                tuple.get(show.viewCount)
        );
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
