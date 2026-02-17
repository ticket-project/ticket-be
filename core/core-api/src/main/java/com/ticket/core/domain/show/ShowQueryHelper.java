package com.ticket.core.domain.show;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.ticket.core.support.cursor.CursorCodec;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import static com.ticket.core.domain.show.QCategory.category;
import static com.ticket.core.domain.show.QGenre.genre;
import static com.ticket.core.domain.show.QShow.show;

/**
 * Show 쿼리에서 공통으로 사용되는 조건/정렬/커서 유틸리티
 */
@Getter
@Component
@RequiredArgsConstructor
public class ShowQueryHelper {

    private final CursorCodec cursorCodec;

    // ========== 정렬 ==========

    public record SortOrder(ShowSortKey key, Sort.Direction direction) {}

    public SortOrder resolveSortOrder(String sort) {
        ShowSortKey sortKey = ShowSortKey.fromApiValue(sort);
        Sort.Direction direction = switch (sortKey) {
            case POPULAR, LATEST -> Sort.Direction.DESC;
            case SHOW_START_APPROACHING, SALE_START_APPROACHING -> Sort.Direction.ASC;
        };
        return new SortOrder(sortKey, direction);
    }

    public OrderSpecifier<?> primaryOrderSpecifier(SortOrder sortOrder) {
        return switch (sortOrder.key()) {
            case POPULAR -> show.viewCount.desc();
            case LATEST -> show.createdAt.desc();
            case SHOW_START_APPROACHING -> show.startDate.asc();
            case SALE_START_APPROACHING -> show.saleStartDate.asc();
        };
    }

    public OrderSpecifier<Long> tieBreakerOrder(SortOrder sortOrder) {
        return sortOrder.direction().isAscending() ? show.id.asc() : show.id.desc();
    }

    // ========== 커서 ==========

    public void applyCursor(BooleanBuilder where, String cursor, SortOrder sortOrder) {
        if (StringUtils.hasText(cursor)) {
            try {
                ShowCursor showCursor = cursorCodec.decode(cursor);
                validateCursorMatchesRequest(showCursor, sortOrder);
                where.and(cursorCondition(showCursor, sortOrder));
            } catch (IllegalArgumentException | DateTimeParseException ex) {
                throw new CoreException(ErrorType.INVALID_REQUEST, "cursor 형식이 올바르지 않습니다.");
            }
        }
    }

    public String buildNextCursor(Long id, ShowSortKey key, Sort.Direction direction, String lastValue) {
        ShowCursor next = new ShowCursor(key, direction.name(), lastValue, id);
        return cursorCodec.encode(next);
    }

    private void validateCursorMatchesRequest(ShowCursor cursor, SortOrder sortOrder) {
        if (!sortOrder.key().equals(cursor.sort())) {
            throw new IllegalArgumentException("cursor.sort와 요청 sort가 일치하지 않습니다.");
        }
        if (!sortOrder.direction().name().equalsIgnoreCase(cursor.dir())) {
            throw new IllegalArgumentException("cursor.dir와 요청 dir가 일치하지 않습니다.");
        }
        if (cursor.lastId() == null) {
            throw new IllegalArgumentException("cursor.lastId가 없습니다.");
        }
        if (!StringUtils.hasText(cursor.lastValue())) {
            throw new IllegalArgumentException("cursor.lastValue가 없습니다.");
        }
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

    // ========== 공통 WHERE 조건 ==========

    public BooleanExpression categoryCodeEq(String categoryCode) {
        return StringUtils.hasText(categoryCode) ? category.code.eq(categoryCode) : null;
    }

    public BooleanExpression genreEq(String genreCode) {
        return StringUtils.hasText(genreCode) ? genre.code.eq(genreCode) : null;
    }

    public BooleanExpression regionEq(Region region) {
        return region != null ? show.venue.region.eq(region) : null;
    }

    public BooleanExpression titleContains(String title) {
        return StringUtils.hasText(title) ? show.title.containsIgnoreCase(title) : null;
    }

    public BooleanExpression keywordContains(String keyword) {
        return titleContains(keyword);
    }

    public BooleanExpression saleStartDateGoe(LocalDate from) {
        return from != null ? show.saleStartDate.goe(from) : null;
    }

    public BooleanExpression saleStartDateLoe(LocalDate to) {
        return to != null ? show.saleStartDate.loe(to) : null;
    }

    public BooleanExpression saleEndDateGoe(LocalDate from) {
        return from != null ? show.saleEndDate.goe(from) : null;
    }

    public BooleanExpression saleEndDateLoe(LocalDate to) {
        return to != null ? show.saleEndDate.loe(to) : null;
    }

    public BooleanExpression startDateGoe(LocalDate from) {
        return from != null ? show.startDate.goe(from) : null;
    }

    public BooleanExpression startDateLoe(LocalDate to) {
        return to != null ? show.startDate.loe(to) : null;
    }

    public BooleanExpression saleStatusCondition(SaleStatus saleStatus) {
        if (saleStatus == null) return null;
        LocalDate today = LocalDate.now();
        return switch (saleStatus) {
            case UPCOMING -> show.saleStartDate.gt(today);
            case ON_SALE -> show.saleStartDate.loe(today).and(show.saleEndDate.goe(today));
            case CLOSED -> show.saleEndDate.lt(today);
        };
    }
}
