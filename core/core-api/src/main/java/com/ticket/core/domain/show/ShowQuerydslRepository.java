package com.ticket.core.domain.show;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticket.core.api.controller.request.SaleOpeningSoonSearchParam;
import com.ticket.core.api.controller.request.ShowParam;
import com.ticket.core.api.controller.request.ShowSearchRequest;
import com.ticket.core.api.controller.response.ShowOpeningSoonDetailResponse;
import com.ticket.core.api.controller.response.ShowOpeningSoonSummaryResponse;
import com.ticket.core.api.controller.response.ShowResponse;
import com.ticket.core.api.controller.response.ShowSearchResponse;
import com.ticket.core.api.controller.response.ShowSummaryResponse;
import com.ticket.core.support.cursor.CursorCodec;
import com.ticket.core.support.cursor.CursorSlice;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.ticket.core.domain.show.QCategory.category;
import static com.ticket.core.domain.show.QGenre.genre;
import static com.ticket.core.domain.show.QShow.show;
import static com.ticket.core.domain.show.QShowGenre.showGenre;

@Repository
public class ShowQuerydslRepository {
    private final JPAQueryFactory queryFactory;
    private final CursorCodec cursorCodec;

    public ShowQuerydslRepository(final JPAQueryFactory queryFactory, final CursorCodec cursorCodec) {
        this.queryFactory = queryFactory;
        this.cursorCodec = cursorCodec;
    }

    public CursorSlice<ShowResponse> findAllBySearch(ShowParam param, int size, String sort) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(categoryCodeEq(param.getCategory()));
        where.and(regionEq(param.getRegion()));
        where.and(genreEq(param.getGenre()));

        SortOrder sortOrder = resolveSortOrder(sort);

        if (ShowSortKey.SHOW_START_APPROACHING.equals(sortOrder.key)) {
            where.and(show.startDate.goe(LocalDate.now()));
        }

        OrderSpecifier<?> primaryOrder = primaryOrderSpecifier(sortOrder);
        OrderSpecifier<Long> tieBreakerOrder = (sortOrder.direction.isAscending() ? show.id.asc() : show.id.desc());

        if (StringUtils.hasText(param.getCursor())) {
            ShowCursor cursor = cursorCodec.decode(param.getCursor());
            validateCursorMatchesRequest(cursor, sortOrder);
            where.and(cursorCondition(cursor, sortOrder));
        }

        List<Tuple> rows = queryFactory
                .select(show.id, show.startDate, show.createdAt, show.viewCount)
                .distinct()
                .from(show)
                .join(showGenre).on(showGenre.show.eq(show))
                .join(genre).on(showGenre.genre.eq(genre))
                .join(category).on(genre.category.eq(category))
                .where(where)
                .orderBy(primaryOrder, tieBreakerOrder)
                .limit(size + 1L)
                .fetch();

        List<Long> ids = rows.stream()
                .map(t -> t.get(show.id))
                .toList();

        if (ids.isEmpty()) {
            return new CursorSlice<>(new SliceImpl<>(List.of(), PageRequest.of(0, size), false), null);
        }

        // 장르 조회
        List<Tuple> genreTuples = queryFactory
                .select(show.id, genre.name)
                .from(show)
                .join(showGenre).on(showGenre.show.eq(show))
                .join(genre).on(showGenre.genre.eq(genre))
                .where(show.id.in(ids))
                .fetch();

        // showId -> genreNames 매핑
        Map<Long, List<String>> genreMap = new LinkedHashMap<>();
        for (Tuple tuple : genreTuples) {
            Long showId = tuple.get(show.id);
            String genreName = tuple.get(genre.name);
            if (genreName != null) {
                genreMap.computeIfAbsent(showId, k -> new ArrayList<>()).add(genreName);
            }
        }

        // Show 정보 조회
        List<Show> shows = queryFactory
                .selectFrom(show)
                .where(show.id.in(ids))
                .orderBy(primaryOrder, tieBreakerOrder)
                .fetch();

        List<ShowResponse> results = shows.stream()
                .map(s -> new ShowResponse(
                        s.getId(),
                        s.getTitle(),
                        s.getSubTitle(),
                        genreMap.getOrDefault(s.getId(), new ArrayList<>()),
                        s.getStartDate(),
                        s.getEndDate(),
                        s.getViewCount(),
                        s.getSaleType(),
                        s.getSaleStartDate(),
                        s.getSaleEndDate(),
                        s.getCreatedAt(),
                        s.getRegion(),
                        s.getVenue()
                ))
                .toList();

        results = new ArrayList<>(results);

        boolean hasNext = results.size() > size;
        if (hasNext) results = results.subList(0, size);

        Slice<ShowResponse> slice = new SliceImpl<>(results, PageRequest.of(0, size), hasNext);

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
            case LATEST -> last.createdAt().toString();
            case SHOW_START_APPROACHING -> last.startDate().toString();
            case SALE_START_APPROACHING -> last.saleStartDate().toString();
        };

        return new ShowCursor(
                sortOrder.key,
                sortOrder.direction.name(),
                lastValue,
                last.id()
        );
    }

    private BooleanExpression cursorCondition(ShowCursor cursor, SortOrder sortOrder) {
        Long lastId = cursor.lastId();

        return switch (sortOrder.key()) {

            case POPULAR -> {
                var last = Long.parseLong(cursor.lastValue());
                yield show.viewCount.lt(last).or(show.viewCount.eq(last).and(show.id.lt(lastId)));
            }
            case LATEST -> {
                var last = LocalDateTime.parse(cursor.lastValue());
                yield show.createdAt.lt(last).or(show.createdAt.eq(last).and(show.id.lt(lastId)));
            }
            case SHOW_START_APPROACHING -> {
                var last = LocalDate.parse(cursor.lastValue());
                yield show.startDate.gt(last).or(show.startDate.eq(last).and(show.id.gt(lastId)));
            }
            case SALE_START_APPROACHING -> {
                var last = LocalDate.parse(cursor.lastValue());
                yield show.saleStartDate.gt(last).or(show.saleStartDate.eq(last).and(show.id.gt(lastId)));
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
            case SHOW_START_APPROACHING -> show.startDate.asc();
            case SALE_START_APPROACHING -> show.saleStartDate.asc();
        };
    }

    private SortOrder resolveSortOrder(String sort) {
        ShowSortKey sortKey = ShowSortKey.fromApiValue(sort);
        // 인기순(POPULAR)은 DESC, 최신순(LATEST)은 DESC, 공연임박순(SHOW_APPROACHING)은 ASC
        Sort.Direction direction = switch (sortKey) {
            case POPULAR, LATEST -> Sort.Direction.DESC;
            case SHOW_START_APPROACHING, SALE_START_APPROACHING -> Sort.Direction.ASC;
        };
        return new SortOrder(sortKey, direction);
    }

    private BooleanExpression categoryCodeEq(String categoryCode) {
        return StringUtils.hasText(categoryCode)
                ? category.code.eq(categoryCode)
                : null;
    }

    private BooleanExpression genreEq(String genreCode) {
        return StringUtils.hasText(genreCode)
                ? genre.code.eq(genreCode)
                : null;
    }

    private BooleanExpression regionEq(Region region) {
        return region != null ? show.region.eq(region) : null;
    }

    private BooleanExpression titleContains(String title) {
        return StringUtils.hasText(title)
                ? show.title.containsIgnoreCase(title)
                : null;
    }

    private BooleanExpression saleStartDateGoe(LocalDate from) {
        return from != null ? show.saleStartDate.goe(from) : null;
    }

    private BooleanExpression saleStartDateLoe(LocalDate to) {
        return to != null ? show.saleStartDate.loe(to) : null;
    }

    private BooleanExpression saleEndDateGoe(LocalDate from) {
        return from != null ? show.saleEndDate.goe(from) : null;
    }

    private BooleanExpression saleEndDateLoe(LocalDate to) {
        return to != null ? show.saleEndDate.loe(to) : null;
    }

    private record SortOrder(ShowSortKey key, Sort.Direction direction) {
    }

    public List<ShowSummaryResponse> findLatestShows(final String categoryCode, int limit) {
        return queryFactory
                .select(Projections.constructor(ShowSummaryResponse.class,
                        show.id,
                        show.title,
                        show.image,
                        show.startDate,
                        show.endDate,
                        show.venue,
                        show.createdAt))
                .distinct()
                .from(show)
                .join(showGenre).on(showGenre.show.eq(show))
                .join(genre).on(showGenre.genre.eq(genre))
                .join(category).on(genre.category.eq(category))
                .where(categoryCodeEq(categoryCode))
                .orderBy(show.createdAt.desc())
                .limit(limit)
                .fetch();
    }

    public List<ShowOpeningSoonSummaryResponse> findShowsSaleOpeningSoon(final String categoryCode, int limit) {
        return queryFactory
                .select(Projections.constructor(ShowOpeningSoonSummaryResponse.class,
                        show.id,
                        show.title,
                        show.image,
                        show.venue,
                        show.saleStartDate
                ))
                .distinct()
                .from(show)
                .join(showGenre).on(showGenre.show.eq(show))
                .join(genre).on(showGenre.genre.eq(genre))
                .join(category).on(genre.category.eq(category))
                .where(
                        categoryCodeEq(categoryCode),
                        saleStartDateGoe(LocalDate.now())
                )
                .orderBy(show.saleStartDate.asc())
                .limit(limit)
                .fetch();
    }

    /**
     * 판매 오픈 예정 공연 무한스크롤 조회
     * - 검색: 제목, 판매시작일, 판매종료일
     * - 정렬: popular(인기순), saleStartApproaching(판매시작일순)
     */
    public CursorSlice<ShowOpeningSoonDetailResponse> findSaleOpeningSoonPage(
            SaleOpeningSoonSearchParam param,
            int size,
            String sort
    ) {
        BooleanBuilder where = new BooleanBuilder();

        // 판매 시작일이 오늘 이후인 것만 (오픈 예정)
        where.and(show.saleStartDate.goe(LocalDate.now()));
        where.and(categoryCodeEq(param.getCategory()));
        where.and(titleContains(param.getTitle()));
        where.and(saleStartDateGoe(param.getSaleStartDateFrom()));
        where.and(saleStartDateLoe(param.getSaleStartDateTo()));
        where.and(saleEndDateGoe(param.getSaleEndDateFrom()));
        where.and(saleEndDateLoe(param.getSaleEndDateTo()));

        // 정렬 결정
        SortOrder sortOrder = resolveSortOrder(sort);
        OrderSpecifier<?> primaryOrder = primaryOrderSpecifier(sortOrder);
        OrderSpecifier<Long> tieBreakerOrder = sortOrder.direction.isAscending() ? show.id.asc() : show.id.desc();

        // 커서 처리
        if (StringUtils.hasText(param.getCursor())) {
            ShowCursor cursor = cursorCodec.decode(param.getCursor());
            validateCursorMatchesRequest(cursor, sortOrder);
            where.and(cursorCondition(cursor, sortOrder));
        }

        // 1차 쿼리: ID 목록 조회
        List<Tuple> rows = queryFactory
                .select(show.id, show.saleStartDate, show.viewCount)
                .distinct()
                .from(show)
                .join(showGenre).on(showGenre.show.eq(show))
                .join(genre).on(showGenre.genre.eq(genre))
                .join(category).on(genre.category.eq(category))
                .where(where)
                .orderBy(primaryOrder, tieBreakerOrder)
                .limit(size + 1L)
                .fetch();

        List<Long> ids = rows.stream()
                .map(t -> t.get(show.id))
                .toList();

        if (ids.isEmpty()) {
            return new CursorSlice<>(new SliceImpl<>(List.of(), PageRequest.of(0, size), false), null);
        }

        // 2차 쿼리: 상세 정보 조회
        List<ShowOpeningSoonDetailResponse> results = queryFactory
                .select(Projections.constructor(ShowOpeningSoonDetailResponse.class,
                        show.id,
                        show.title,
                        show.subTitle,
                        show.image,
                        show.venue,
                        show.region,
                        show.startDate,
                        show.endDate,
                        show.saleStartDate,
                        show.saleEndDate,
                        show.viewCount
                ))
                .from(show)
                .where(show.id.in(ids))
                .orderBy(primaryOrder, tieBreakerOrder)
                .fetch();

        boolean hasNext = results.size() > size;
        if (hasNext) {
            results = results.subList(0, size);
        }

        Slice<ShowOpeningSoonDetailResponse> slice = new SliceImpl<>(results, PageRequest.of(0, size), hasNext);

        String nextCursor = null;
        if (hasNext && !results.isEmpty()) {
            ShowOpeningSoonDetailResponse last = results.getLast();
            String lastValue = sortOrder.key() == ShowSortKey.POPULAR
                    ? String.valueOf(last.viewCount())
                    : last.saleStartDate().toString();
            ShowCursor next = new ShowCursor(
                    sortOrder.key(),
                    sortOrder.direction().name(),
                    lastValue,
                    last.id()
            );
            nextCursor = cursorCodec.encode(next);
        }

        return new CursorSlice<>(slice, nextCursor);
    }

    // ========== 검색 API 메서드 ==========

    /**
     * 공연 검색 (커서 기반 페이지네이션)
     * - 검색: 키워드(공연명)
     * - 필터: 카테고리, 판매상태, 날짜범위, 지역
     * - 정렬: popular(조회순, 기본), showStartApproaching(공연임박순)
     */
    public CursorSlice<ShowSearchResponse> searchShows(
            ShowSearchRequest request,
            int size,
            String sort
    ) {
        BooleanBuilder where = buildSearchCondition(request);

        SortOrder sortOrder = resolveSortOrder(sort);
        OrderSpecifier<?> primaryOrder = primaryOrderSpecifier(sortOrder);
        OrderSpecifier<Long> tieBreakerOrder = sortOrder.direction.isAscending() ? show.id.asc() : show.id.desc();

        // 공연임박순은 오늘 이후 공연만 조회
        if (ShowSortKey.SHOW_START_APPROACHING.equals(sortOrder.key)) {
            where.and(show.startDate.goe(LocalDate.now()));
        }

        // 커서 처리
        if (StringUtils.hasText(request.getCursor())) {
            ShowCursor cursor = cursorCodec.decode(request.getCursor());
            validateCursorMatchesRequest(cursor, sortOrder);
            where.and(cursorCondition(cursor, sortOrder));
        }

        // 1차 쿼리: ID 목록 조회
        List<Tuple> rows = queryFactory
                .select(show.id, show.startDate, show.viewCount)
                .distinct()
                .from(show)
                .join(showGenre).on(showGenre.show.eq(show))
                .join(genre).on(showGenre.genre.eq(genre))
                .join(category).on(genre.category.eq(category))
                .where(where)
                .orderBy(primaryOrder, tieBreakerOrder)
                .limit(size + 1L)
                .fetch();

        List<Long> ids = rows.stream()
                .map(t -> t.get(show.id))
                .toList();

        if (ids.isEmpty()) {
            return new CursorSlice<>(new SliceImpl<>(List.of(), PageRequest.of(0, size), false), null);
        }

        // 2차 쿼리: 상세 정보 조회
        List<ShowSearchResponse> results = queryFactory
                .select(Projections.constructor(ShowSearchResponse.class,
                        show.id,
                        show.title,
                        show.image,
                        show.venue,
                        show.startDate,
                        show.endDate,
                        show.region,
                        show.viewCount
                ))
                .from(show)
                .where(show.id.in(ids))
                .orderBy(primaryOrder, tieBreakerOrder)
                .fetch();

        boolean hasNext = results.size() > size;
        if (hasNext) {
            results = results.subList(0, size);
        }

        Slice<ShowSearchResponse> slice = new SliceImpl<>(results, PageRequest.of(0, size), hasNext);

        String nextCursor = null;
        if (hasNext && !results.isEmpty()) {
            ShowSearchResponse last = results.getLast();
            String lastValue = sortOrder.key() == ShowSortKey.POPULAR
                    ? String.valueOf(last.viewCount())
                    : last.startDate().toString();
            ShowCursor next = new ShowCursor(
                    sortOrder.key(),
                    sortOrder.direction().name(),
                    lastValue,
                    last.id()
            );
            nextCursor = cursorCodec.encode(next);
        }

        return new CursorSlice<>(slice, nextCursor);
    }

    /**
     * 공연 검색 결과 개수 조회 (필터 선택 시 사용)
     */
    public long countSearchShows(ShowSearchRequest request) {
        BooleanBuilder where = buildSearchCondition(request);

        Long count = queryFactory
                .select(show.id.countDistinct())
                .from(show)
                .join(showGenre).on(showGenre.show.eq(show))
                .join(genre).on(showGenre.genre.eq(genre))
                .join(category).on(genre.category.eq(category))
                .where(where)
                .fetchOne();

        return count != null ? count : 0L;
    }

    /**
     * 검색 조건 빌더 (공통)
     */
    private BooleanBuilder buildSearchCondition(ShowSearchRequest request) {
        BooleanBuilder where = new BooleanBuilder();

        // 키워드 검색 (공연명)
        where.and(keywordContains(request.getKeyword()));
        // 카테고리 필터
        where.and(categoryCodeEq(request.getCategory()));
        // 지역 필터
        where.and(regionEq(request.getRegion()));
        // 공연 시작일 범위
        where.and(startDateGoe(request.getStartDateFrom()));
        where.and(startDateLoe(request.getStartDateTo()));
        // 판매 상태 필터
        where.and(saleStatusCondition(request.getSaleStatus()));

        return where;
    }

    /**
     * 키워드 검색 조건 (공연명)
     */
    private BooleanExpression keywordContains(String keyword) {
        return StringUtils.hasText(keyword)
                ? show.title.containsIgnoreCase(keyword)
                : null;
    }

    /**
     * 공연 시작일 이후 조건
     */
    private BooleanExpression startDateGoe(LocalDate from) {
        return from != null ? show.startDate.goe(from) : null;
    }

    /**
     * 공연 시작일 이전 조건
     */
    private BooleanExpression startDateLoe(LocalDate to) {
        return to != null ? show.startDate.loe(to) : null;
    }

    /**
     * 판매 상태에 따른 조건 생성
     * - UPCOMING: saleStartDate > today
     * - ON_SALE: saleStartDate <= today <= saleEndDate
     * - CLOSED: saleEndDate < today
     */
    private BooleanExpression saleStatusCondition(SaleStatus saleStatus) {
        if (saleStatus == null) {
            return null;
        }

        LocalDate today = LocalDate.now();

        return switch (saleStatus) {
            case UPCOMING -> show.saleStartDate.gt(today);
            case ON_SALE -> show.saleStartDate.loe(today).and(show.saleEndDate.goe(today));
            case CLOSED -> show.saleEndDate.lt(today);
        };
    }

}
