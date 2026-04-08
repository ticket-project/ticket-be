package com.ticket.core.domain.show.query;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticket.core.domain.show.image.ShowCardImagePathConverter;
import com.ticket.core.domain.show.query.model.ShowSummaryView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import static com.ticket.core.domain.show.mapping.QShowGenre.showGenre;
import static com.ticket.core.domain.show.model.QGenre.genre;
import static com.ticket.core.domain.show.model.QShow.show;
import static com.ticket.core.domain.show.venue.QVenue.venue;
import static com.ticket.core.domain.showlike.model.QShowLike.showLike;

@Repository
@RequiredArgsConstructor
public class ShowRecommendationQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final ShowCardImagePathConverter showCardImagePathConverter;
    private final Clock clock;

    public List<ShowSummaryView> findRecommendedShows(final Long memberId, final int limit) {
        final List<Long> likedShowIds = queryFactory
                .select(showLike.show.id)
                .from(showLike)
                .where(showLike.member.id.eq(memberId))
                .fetch();

        final List<Long> preferredGenreIds = likedShowIds.isEmpty()
                ? List.of()
                : queryFactory
                .select(showGenre.genre.id)
                .distinct()
                .from(showGenre)
                .where(showGenre.show.id.in(likedShowIds))
                .fetch();

        if (preferredGenreIds.isEmpty()) {
            return findPopularShows(limit);
        }

        final List<Tuple> rows = queryFactory
                .select(show.id, show.title, show.image, show.startDate, show.endDate, venue.name, show.createdAt)
                .distinct()
                .from(show)
                .leftJoin(show.venue, venue)
                .leftJoin(showGenre).on(showGenre.show.eq(show))
                .where(
                        showGenre.genre.id.in(preferredGenreIds)
                                .and(show.id.notIn(likedShowIds))
                                .and(show.saleEndDate.goe(LocalDateTime.now(clock)))
                )
                .orderBy(show.viewCount.desc(), show.id.desc())
                .limit(limit)
                .fetch();

        return rows.stream().map(this::toShowSummaryView).toList();
    }

    private List<ShowSummaryView> findPopularShows(final int limit) {
        return queryFactory
                .select(show.id, show.title, show.image, show.startDate, show.endDate, venue.name, show.createdAt)
                .distinct()
                .from(show)
                .leftJoin(show.venue, venue)
                .where(show.saleEndDate.goe(LocalDateTime.now(clock)))
                .orderBy(show.viewCount.desc(), show.id.desc())
                .limit(limit)
                .fetch()
                .stream()
                .map(this::toShowSummaryView)
                .toList();
    }

    private ShowSummaryView toShowSummaryView(final Tuple tuple) {
        return new ShowSummaryView(
                tuple.get(show.id),
                tuple.get(show.title),
                showCardImagePathConverter.toCardImage(tuple.get(show.image)),
                tuple.get(show.startDate),
                tuple.get(show.endDate),
                tuple.get(venue.name),
                tuple.get(show.createdAt)
        );
    }
}
