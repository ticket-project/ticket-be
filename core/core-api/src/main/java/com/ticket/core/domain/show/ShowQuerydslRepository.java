package com.ticket.core.domain.show;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticket.core.api.controller.request.ShowSearchParam;
import com.ticket.core.api.controller.response.ShowResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.ticket.core.domain.show.QCategory.category;
import static com.ticket.core.domain.show.QShow.show;
import static com.ticket.core.domain.show.QShowCategory.showCategory;

@Repository
public class ShowQuerydslRepository {
    private final JPAQueryFactory queryFactory;

    public ShowQuerydslRepository(final JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public Slice<ShowResponse> findAllBySearch(ShowSearchParam param, Pageable pageable) {
        List<ShowResponse> results = queryFactory
                .select(Projections.constructor(ShowResponse.class,
                        show.id,
                        show.title,
                        show.subTitle,
                        show.startDate,
                        show.endDate,
                        show.place))
                .from(show)
                .leftJoin(showCategory).on(showCategory.show.eq(show))
                .leftJoin(category).on(showCategory.category.eq(category))
                .where(
                        categoryNameEq(param.getCategory()),
                        placeEq(param.getPlace())
                )
                .distinct()
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .orderBy(getOrderSpecifiers(pageable.getSort()))
                .fetch();

        boolean hasNext = results.size() > pageable.getPageSize();
        if (hasNext) {
            results = new ArrayList<>(results);
            results.removeLast();
        }

        return new SliceImpl<>(results, pageable, hasNext);
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(Sort sort) {
        if (sort.isEmpty()) {
            return new OrderSpecifier<?>[] { new OrderSpecifier<>(Order.DESC, show.startDate) };
        }

        return sort.stream()
                .map(order -> {
                    Order direction = order.isAscending() ? Order.ASC : Order.DESC;
                    return switch (order.getProperty()) {
                        case "startDate" -> new OrderSpecifier<>(direction, show.startDate);
                        case "endDate" -> new OrderSpecifier<>(direction, show.endDate);
                        case "title" -> new OrderSpecifier<>(direction, show.title);
                        case "id" -> new OrderSpecifier<>(direction, show.id);
                        default -> new OrderSpecifier<>(Order.DESC, show.startDate);
                    };
                })
                .toArray(OrderSpecifier[]::new);
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

}
