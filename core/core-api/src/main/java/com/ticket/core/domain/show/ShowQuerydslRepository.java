package com.ticket.core.domain.show;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticket.core.api.controller.request.SaleOpeningSoonSearchParam;
import com.ticket.core.api.controller.request.ShowSearchParam;
import com.ticket.core.api.controller.response.ShowOpeningSoonDetailResponse;
import com.ticket.core.api.controller.response.ShowOpeningSoonSummaryResponse;
import com.ticket.core.api.controller.response.ShowResponse;
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
import static com.ticket.core.domain.show.QShow.show;
import static com.ticket.core.domain.show.QShowCategory.showCategory;

@Repository
public class ShowQuerydslRepository {
    private final JPAQueryFactory queryFactory;
    private final CursorCodec cursorCodec;

    public ShowQuerydslRepository(final JPAQueryFactory queryFactory, final CursorCodec cursorCodec) {
        this.queryFactory = queryFactory;
        this.cursorCodec = cursorCodec;
    }

    public CursorSlice<ShowResponse> findAllBySearch(ShowSearchParam param, int size, String sort) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(categoryCodeEq(param.getCategory()));
        where.and(regionEq(param.getRegion()));

        SortOrder sortOrder = resolveSortOrder(sort);

        if (ShowSortKey.SHOW_APPROACHING.equals(sortOrder.key)) {
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
                .leftJoin(showCategory).on(showCategory.show.eq(show))
                .leftJoin(category).on(showCategory.category.eq(category))
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

        List<Tuple> tuples = queryFactory
                .select(show, category.name)
                .from(show)
                .leftJoin(showCategory).on(showCategory.show.eq(show))
                .leftJoin(category).on(showCategory.category.eq(category))
                .where(show.id.in(ids))
                .orderBy(primaryOrder, tieBreakerOrder)
                .fetch();

        Map<Long, ShowResponse> resultMap = new LinkedHashMap<>();
        for (Tuple tuple : tuples) {
            Show s = tuple.get(show);
            String catName = tuple.get(category.name);

            resultMap.computeIfAbsent(s.getId(), id -> new ShowResponse(
                    s.getId(),
                    s.getTitle(),
                    s.getSubTitle(),
                    new ArrayList<>(),
                    s.getStartDate(),
                    s.getEndDate(),
                    s.getViewCount(),
                    s.getSaleType(),
                    s.getSaleStartDate(),
                    s.getSaleEndDate(),
                    s.getCreatedAt(),
                    s.getRegion(),
                    s.getVenue()
            ));
            if (catName != null) {
                resultMap.get(s.getId()).categoryNames().add(catName);
            }
        }

        List<ShowResponse> results = new ArrayList<>(resultMap.values());

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
            case SHOW_APPROACHING -> last.startDate().toString();
            case SALE_OPENING -> throw new UnsupportedOperationException("SALE_OPENING은 이 메서드에서 사용 불가");
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
            case SHOW_APPROACHING -> {
                var last = LocalDate.parse(cursor.lastValue());
                yield show.startDate.gt(last).or(show.startDate.eq(last).and(show.id.gt(lastId)));
            }
            case SALE_OPENING -> throw new UnsupportedOperationException("SALE_OPENING은 이 메서드에서 사용 불가");
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
            case SHOW_APPROACHING -> show.startDate.asc();
            case SALE_OPENING -> show.saleStartDate.asc();
        };
    }

    private SortOrder resolveSortOrder(String sort) {
        ShowSortKey sortKey = ShowSortKey.fromApiValue(sort);
        // 인기순(POPULAR)은 DESC, 최신순(LATEST)은 DESC, 공연임박순(SHOW_APPROACHING)은 ASC
        Sort.Direction direction = switch (sortKey) {
            case POPULAR, LATEST -> Sort.Direction.DESC;
            case SHOW_APPROACHING, SALE_OPENING -> Sort.Direction.ASC;
        };
        return new SortOrder(sortKey, direction);
    }

    private BooleanExpression categoryCodeEq(String categoryCode) {
        return StringUtils.hasText(categoryCode)
                ? category.code.eq(categoryCode)
                : null;
    }

    private BooleanExpression regionEq(Region region) {
        return region != null ? show.region.eq(region) : null;
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
                        show.venue,
                        show.createdAt))
                .distinct()
                .from(show)
                .join(showCategory).on(showCategory.show.eq(show))
                .join(category).on(category.eq(showCategory.category))
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
                .join(showCategory).on(showCategory.show.eq(show))
                .join(category).on(category.eq(showCategory.category))
                .where(category.code.eq(categoryCode)
                        .and(show.saleStartDate.goe(LocalDate.now())))
                .orderBy(show.saleStartDate.asc())
                .limit(limit)
                .fetch();
    }

    /**
     * 판매 오픈 예정 공연 무한스크롤 조회
     * - 검색: 제목, 판매시작일, 판매종료일
     * - 정렬: popular(인기순), saleStartDate(판매시작일순)
     */
    public CursorSlice<ShowOpeningSoonDetailResponse> findSaleOpeningSoonPage(
            SaleOpeningSoonSearchParam param,
            int size,
            String sort
    ) {
        BooleanBuilder where = new BooleanBuilder();
        
        // 판매 시작일이 오늘 이후인 것만 (오픈 예정)
        where.and(show.saleStartDate.goe(LocalDate.now()));
        
        // 카테고리 필터
        if (StringUtils.hasText(param.getCategory())) {
            where.and(category.code.eq(param.getCategory()));
        }
        
        // 제목 검색 (부분 일치)
        if (StringUtils.hasText(param.getTitle())) {
            where.and(show.title.containsIgnoreCase(param.getTitle()));
        }
        
        // 판매 시작일 범위 필터
        if (param.getSaleStartDateFrom() != null) {
            where.and(show.saleStartDate.goe(param.getSaleStartDateFrom()));
        }
        if (param.getSaleStartDateTo() != null) {
            where.and(show.saleStartDate.loe(param.getSaleStartDateTo()));
        }
        
        // 판매 종료일 범위 필터
        if (param.getSaleEndDateFrom() != null) {
            where.and(show.saleEndDate.goe(param.getSaleEndDateFrom()));
        }
        if (param.getSaleEndDateTo() != null) {
            where.and(show.saleEndDate.loe(param.getSaleEndDateTo()));
        }
        
        // 정렬 결정 (popular: 인기순, saleStartDate: 판매시작일순 - 기본값)
        boolean isPopularSort = "popular".equalsIgnoreCase(sort);
        OrderSpecifier<?> primaryOrder = isPopularSort ? show.viewCount.desc() : show.saleStartDate.asc();
        OrderSpecifier<Long> tieBreakerOrder = isPopularSort ? show.id.desc() : show.id.asc();
        
        // 커서 처리
        if (StringUtils.hasText(param.getCursor())) {
            ShowCursor cursor = cursorCodec.decode(param.getCursor());
            where.and(saleOpeningSoonCursorCondition(cursor, isPopularSort));
        }
        
        // 1차 쿼리: ID 목록 조회
        List<Tuple> rows = queryFactory
                .select(show.id, show.saleStartDate, show.viewCount)
                .distinct()
                .from(show)
                .leftJoin(showCategory).on(showCategory.show.eq(show))
                .leftJoin(category).on(showCategory.category.eq(category))
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
                        show.region.stringValue(),
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
            String lastValue = isPopularSort 
                    ? String.valueOf(last.viewCount())
                    : last.saleStartDate().toString();
            ShowCursor next = new ShowCursor(
                    isPopularSort ? ShowSortKey.POPULAR : ShowSortKey.SALE_OPENING,
                    isPopularSort ? "DESC" : "ASC",
                    lastValue,
                    last.id()
            );
            nextCursor = cursorCodec.encode(next);
        }
        
        return new CursorSlice<>(slice, nextCursor);
    }
    
    private BooleanExpression saleOpeningSoonCursorCondition(ShowCursor cursor, boolean isPopularSort) {
        Long lastId = cursor.lastId();
        
        if (isPopularSort) {
            var lastViewCount = Long.parseLong(cursor.lastValue());
            return show.viewCount.lt(lastViewCount)
                    .or(show.viewCount.eq(lastViewCount).and(show.id.lt(lastId)));
        } else {
            var lastSaleStartDate = LocalDate.parse(cursor.lastValue());
            return show.saleStartDate.gt(lastSaleStartDate)
                    .or(show.saleStartDate.eq(lastSaleStartDate).and(show.id.gt(lastId)));
        }
    }

}
