package com.ticket.core.domain.show;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticket.core.api.controller.request.SaleOpeningSoonSearchParam;
import com.ticket.core.api.controller.request.ShowParam;
import com.ticket.core.api.controller.request.ShowSearchRequest;
import com.ticket.core.api.controller.response.ShowOpeningSoonDetailResponse;
import com.ticket.core.api.controller.response.ShowOpeningSoonSummaryResponse;
import com.ticket.core.api.controller.response.ShowResponse;
import com.ticket.core.api.controller.response.ShowSearchResponse;
import com.ticket.core.api.controller.response.ShowSummaryResponse;
import com.ticket.core.domain.show.ShowQueryHelper.SortOrder;
import com.ticket.core.support.cursor.CursorSlice;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public ShowListQueryRepository(JPAQueryFactory queryFactory, ShowQueryHelper queryHelper) {
        this.queryFactory = queryFactory;
        this.queryHelper = queryHelper;
    }

    // ========== 메인 페이지 API ==========

    public CursorSlice<ShowResponse> findAllBySearch(ShowParam param, int size, String sort) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(queryHelper.categoryCodeEq(param.getCategory()));
        where.and(queryHelper.regionEq(param.getRegion()));
        where.and(queryHelper.genreEq(param.getGenre()));

        SortOrder sortOrder = queryHelper.resolveSortOrder(sort);

        if (ShowSortKey.SHOW_START_APPROACHING.equals(sortOrder.key())) {
            where.and(show.startDate.goe(LocalDate.now()));
        }

        OrderSpecifier<?> primaryOrder = queryHelper.primaryOrderSpecifier(sortOrder);
        OrderSpecifier<Long> tieBreakerOrder = queryHelper.tieBreakerOrder(sortOrder);

        queryHelper.applyCursor(where, param.getCursor(), sortOrder);

        List<Tuple> rows = queryFactory
                .select(show.id, show.startDate, show.createdAt, show.viewCount)
                .distinct()
                .from(show)
                .leftJoin(showGenre).on(showGenre.show.eq(show))
                .leftJoin(genre).on(showGenre.genre.eq(genre))
                .leftJoin(category).on(genre.category.eq(category))
                .where(where)
                .orderBy(primaryOrder, tieBreakerOrder)
                .limit(size + 1L)
                .fetch();

        List<Long> ids = rows.stream().map(t -> t.get(show.id)).toList();

        if (ids.isEmpty()) {
            return new CursorSlice<>(new SliceImpl<>(List.of(), PageRequest.of(0, size), false), null);
        }

        // 장르 조회
        Map<Long, List<String>> genreMap = fetchGenreMap(ids);

        // Show 정보 조회
        List<Show> shows = queryFactory
                .selectFrom(show)
                .leftJoin(show.venue, venue).fetchJoin()
                .where(show.id.in(ids))
                .orderBy(primaryOrder, tieBreakerOrder)
                .fetch();

        List<ShowResponse> results = new ArrayList<>(shows.stream()
                .map(s -> new ShowResponse(
                        s.getId(), s.getTitle(), s.getSubTitle(),
                        genreMap.getOrDefault(s.getId(), new ArrayList<>()),
                        s.getStartDate(), s.getEndDate(), s.getViewCount(),
                        s.getSaleType(), s.getSaleStartDate(), s.getSaleEndDate(),
                        s.getCreatedAt(),
                        s.getVenue() != null ? s.getVenue().getRegion() : null,
                        s.getVenue() != null ? s.getVenue().getName() : null))
                .toList());

        boolean hasNext = results.size() > size;
        if (hasNext) results = results.subList(0, size);

        Slice<ShowResponse> slice = new SliceImpl<>(results, PageRequest.of(0, size), hasNext);

        String nextCursor = null;
        if (hasNext && !results.isEmpty()) {
            ShowResponse last = results.getLast();
            String lastValue = resolveLastValue(last, sortOrder);
            nextCursor = queryHelper.buildNextCursor(last.id(), sortOrder.key(), sortOrder.direction(), lastValue);
        }

        return new CursorSlice<>(slice, nextCursor);
    }

    private String resolveLastValue(ShowResponse last, SortOrder sortOrder) {
        return switch (sortOrder.key()) {
            case POPULAR -> last.viewCount().toString();
            case LATEST -> last.createdAt().toString();
            case SHOW_START_APPROACHING -> last.startDate().toString();
            case SALE_START_APPROACHING -> last.saleStartDate().toString();
        };
    }

    public List<ShowSummaryResponse> findLatestShows(String categoryCode, int limit) {
        return queryFactory
                .select(Projections.constructor(ShowSummaryResponse.class,
                        show.id, show.title, show.image, show.startDate, show.endDate, show.venue.name, show.createdAt))
                .distinct()
                .from(show)
                .leftJoin(showGenre).on(showGenre.show.eq(show))
                .leftJoin(genre).on(showGenre.genre.eq(genre))
                .leftJoin(category).on(genre.category.eq(category))
                .where(queryHelper.categoryCodeEq(categoryCode))
                .orderBy(show.createdAt.desc())
                .limit(limit)
                .fetch();
    }

    public List<ShowOpeningSoonSummaryResponse> findShowsSaleOpeningSoon(String categoryCode, int limit) {
        return queryFactory
                .select(Projections.constructor(ShowOpeningSoonSummaryResponse.class,
                        show.id, show.title, show.image, show.venue.name, show.saleStartDate))
                .distinct()
                .from(show)
                .leftJoin(showGenre).on(showGenre.show.eq(show))
                .leftJoin(genre).on(showGenre.genre.eq(genre))
                .leftJoin(category).on(genre.category.eq(category))
                .where(queryHelper.categoryCodeEq(categoryCode), queryHelper.saleStartDateGoe(LocalDate.now()))
                .orderBy(show.saleStartDate.asc())
                .limit(limit)
                .fetch();
    }

    public CursorSlice<ShowOpeningSoonDetailResponse> findSaleOpeningSoonPage(
            SaleOpeningSoonSearchParam param, int size, String sort) {

        BooleanBuilder where = new BooleanBuilder();
        where.and(show.saleStartDate.goe(LocalDate.now()));
        where.and(queryHelper.categoryCodeEq(param.getCategory()));
        where.and(queryHelper.titleContains(param.getTitle()));
        where.and(queryHelper.saleStartDateGoe(param.getSaleStartDateFrom()));
        where.and(queryHelper.saleStartDateLoe(param.getSaleStartDateTo()));
        where.and(queryHelper.saleEndDateGoe(param.getSaleEndDateFrom()));
        where.and(queryHelper.saleEndDateLoe(param.getSaleEndDateTo()));

        SortOrder sortOrder = queryHelper.resolveSortOrder(sort);
        OrderSpecifier<?> primaryOrder = queryHelper.primaryOrderSpecifier(sortOrder);
        OrderSpecifier<Long> tieBreakerOrder = queryHelper.tieBreakerOrder(sortOrder);

        queryHelper.applyCursor(where, param.getCursor(), sortOrder);

        List<Tuple> rows = queryFactory
                .select(show.id, show.saleStartDate, show.viewCount)
                .distinct()
                .from(show)
                .leftJoin(showGenre).on(showGenre.show.eq(show))
                .leftJoin(genre).on(showGenre.genre.eq(genre))
                .leftJoin(category).on(genre.category.eq(category))
                .where(where)
                .orderBy(primaryOrder, tieBreakerOrder)
                .limit(size + 1L)
                .fetch();

        List<Long> ids = rows.stream().map(t -> t.get(show.id)).toList();
        if (ids.isEmpty()) {
            return new CursorSlice<>(new SliceImpl<>(List.of(), PageRequest.of(0, size), false), null);
        }

        List<ShowOpeningSoonDetailResponse> results = queryFactory
                .select(Projections.constructor(ShowOpeningSoonDetailResponse.class,
                        show.id, show.title, show.subTitle, show.image, show.venue.name, show.venue.region,
                        show.startDate, show.endDate, show.saleStartDate, show.saleEndDate, show.viewCount))
                .from(show)
                .where(show.id.in(ids))
                .orderBy(primaryOrder, tieBreakerOrder)
                .fetch();

        boolean hasNext = results.size() > size;
        if (hasNext) results = results.subList(0, size);

        Slice<ShowOpeningSoonDetailResponse> slice = new SliceImpl<>(results, PageRequest.of(0, size), hasNext);

        String nextCursor = null;
        if (hasNext && !results.isEmpty()) {
            ShowOpeningSoonDetailResponse last = results.getLast();
            String lastValue = sortOrder.key() == ShowSortKey.POPULAR
                    ? String.valueOf(last.viewCount()) : last.saleStartDate().toString();
            nextCursor = queryHelper.buildNextCursor(last.id(), sortOrder.key(), sortOrder.direction(), lastValue);
        }

        return new CursorSlice<>(slice, nextCursor);
    }

    // ========== 검색 API ==========

    public CursorSlice<ShowSearchResponse> searchShows(ShowSearchRequest request, int size, String sort) {
        BooleanBuilder where = buildSearchCondition(request);

        SortOrder sortOrder = queryHelper.resolveSortOrder(sort);
        OrderSpecifier<?> primaryOrder = queryHelper.primaryOrderSpecifier(sortOrder);
        OrderSpecifier<Long> tieBreakerOrder = queryHelper.tieBreakerOrder(sortOrder);

        if (ShowSortKey.SHOW_START_APPROACHING.equals(sortOrder.key())) {
            where.and(show.startDate.goe(LocalDate.now()));
        }

        queryHelper.applyCursor(where, request.getCursor(), sortOrder);

        List<Tuple> rows = queryFactory
                .select(show.id, show.startDate, show.viewCount)
                .distinct()
                .from(show)
                .leftJoin(showGenre).on(showGenre.show.eq(show))
                .leftJoin(genre).on(showGenre.genre.eq(genre))
                .leftJoin(category).on(genre.category.eq(category))
                .where(where)
                .orderBy(primaryOrder, tieBreakerOrder)
                .limit(size + 1L)
                .fetch();

        List<Long> ids = rows.stream().map(t -> t.get(show.id)).toList();
        if (ids.isEmpty()) {
            return new CursorSlice<>(new SliceImpl<>(List.of(), PageRequest.of(0, size), false), null);
        }

        List<ShowSearchResponse> results = queryFactory
                .select(Projections.constructor(ShowSearchResponse.class,
                        show.id, show.title, show.image, show.venue.name,
                        show.startDate, show.endDate, show.venue.region, show.viewCount))
                .from(show)
                .where(show.id.in(ids))
                .orderBy(primaryOrder, tieBreakerOrder)
                .fetch();

        boolean hasNext = results.size() > size;
        if (hasNext) results = results.subList(0, size);

        Slice<ShowSearchResponse> slice = new SliceImpl<>(results, PageRequest.of(0, size), hasNext);

        String nextCursor = null;
        if (hasNext && !results.isEmpty()) {
            ShowSearchResponse last = results.getLast();
            String lastValue = sortOrder.key() == ShowSortKey.POPULAR
                    ? String.valueOf(last.viewCount()) : last.startDate().toString();
            nextCursor = queryHelper.buildNextCursor(last.id(), sortOrder.key(), sortOrder.direction(), lastValue);
        }

        return new CursorSlice<>(slice, nextCursor);
    }

    public long countSearchShows(ShowSearchRequest request) {
        BooleanBuilder where = buildSearchCondition(request);
        Long count = queryFactory
                .select(show.id.countDistinct())
                .from(show)
                .leftJoin(showGenre).on(showGenre.show.eq(show))
                .leftJoin(genre).on(showGenre.genre.eq(genre))
                .leftJoin(category).on(genre.category.eq(category))
                .where(where)
                .fetchOne();
        return count != null ? count : 0L;
    }

    private BooleanBuilder buildSearchCondition(ShowSearchRequest request) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(queryHelper.keywordContains(request.getKeyword()));
        where.and(queryHelper.categoryCodeEq(request.getCategory()));
        where.and(queryHelper.regionEq(request.getRegion()));
        where.and(queryHelper.startDateGoe(request.getStartDateFrom()));
        where.and(queryHelper.startDateLoe(request.getStartDateTo()));
        where.and(queryHelper.saleStatusCondition(request.getSaleStatus()));
        return where;
    }

    // ========== 내부 헬퍼 ==========

    private Map<Long, List<String>> fetchGenreMap(List<Long> ids) {
        List<Tuple> genreTuples = queryFactory
                .select(show.id, genre.name)
                .from(show)
                .leftJoin(showGenre).on(showGenre.show.eq(show))
                .leftJoin(genre).on(showGenre.genre.eq(genre))
                .where(show.id.in(ids))
                .fetch();

        Map<Long, List<String>> genreMap = new LinkedHashMap<>();
        for (Tuple tuple : genreTuples) {
            Long showId = tuple.get(show.id);
            String genreName = tuple.get(genre.name);
            if (genreName != null) {
                genreMap.computeIfAbsent(showId, k -> new ArrayList<>()).add(genreName);
            }
        }
        return genreMap;
    }
}
