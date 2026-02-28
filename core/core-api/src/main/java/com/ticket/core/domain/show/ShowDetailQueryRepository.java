package com.ticket.core.domain.show;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticket.core.api.controller.response.ShowDetailResponse;
import com.ticket.core.api.controller.response.ShowDetailResponse.GradeInfo;
import com.ticket.core.api.controller.response.ShowDetailResponse.PerformanceDateInfo;
import com.ticket.core.api.controller.response.ShowDetailResponse.PerformanceInfo;
import com.ticket.core.api.controller.response.ShowDetailResponse.PerformerInfo;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.enums.BookingStatus;
import com.ticket.core.enums.EntityStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ticket.core.domain.performance.QPerformance.performance;
import static com.ticket.core.domain.show.QGenre.genre;
import static com.ticket.core.domain.show.QPerformer.performer;
import static com.ticket.core.domain.show.QShow.show;
import static com.ticket.core.domain.show.QShowGenre.showGenre;
import static com.ticket.core.domain.show.QShowGrade.showGrade;
import static com.ticket.core.domain.showlike.QShowLike.showLike;

@Repository
public class ShowDetailQueryRepository {

    private final JPAQueryFactory queryFactory;

    public ShowDetailQueryRepository(final JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public Optional<ShowDetailResponse> findShowDetail(final Long showId) {
        final Show showEntity = queryFactory
                .selectFrom(show)
                .leftJoin(show.performer, performer).fetchJoin()
                .leftJoin(show.venue).fetchJoin()
                .where(show.id.eq(showId))
                .fetchOne();

        if (showEntity == null) {
            return Optional.empty();
        }

        final List<String> genreNames = queryFactory
                .select(genre.name)
                .from(showGenre)
                .join(showGenre.genre, genre)
                .where(showGenre.show.id.eq(showId))
                .fetch();

        final List<ShowGrade> gradeEntities = queryFactory
                .selectFrom(showGrade)
                .where(showGrade.show.id.eq(showId))
                .orderBy(showGrade.sortOrder.asc())
                .fetch();

        final List<GradeInfo> grades = gradeEntities.stream()
                .map(g -> new GradeInfo(g.getId(), g.getGradeCode(), g.getGradeName(), g.getPrice(), g.getSortOrder()))
                .toList();

        final List<Performance> performanceEntities = queryFactory
                .selectFrom(performance)
                .where(performance.show.id.eq(showId))
                .orderBy(performance.startTime.asc(), performance.performanceNo.asc())
                .fetch();

        final BookingStatus showBookingStatus = showEntity.getBookingStatus(LocalDateTime.now());
        final List<PerformanceInfo> performances = performanceEntities.stream()
                .map(p -> new PerformanceInfo(
                        p.getId(), p.getPerformanceNo(), p.getStartTime(), p.getEndTime(),
                        p.getOrderOpenTime(), p.getOrderCloseTime(), p.getState()))
                .toList();

        final List<PerformanceDateInfo> performanceDates = performances.stream()
                .collect(Collectors.groupingBy(
                        performanceInfo -> performanceInfo.startTime().toLocalDate(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ))
                .entrySet().stream()
                .map(entry -> new PerformanceDateInfo(entry.getKey(), entry.getValue()))
                .toList();

        final long likeCount = Optional.ofNullable(
                queryFactory.select(showLike.count())
                        .from(showLike)
                        .where(
                                showLike.show.id.eq(showId),
                                showLike.status.eq(EntityStatus.ACTIVE)
                        )
                        .fetchOne()
        ).orElse(0L);

        final Performer performerEntity = showEntity.getPerformer();
        final PerformerInfo performerInfo = performerEntity != null
                ? new PerformerInfo(performerEntity.getId(), performerEntity.getName(), performerEntity.getProfileImageUrl())
                : null;

        final Venue venueEntity = showEntity.getVenue();
        final ShowDetailResponse.VenueInfo venueInfo = venueEntity != null
                ? new ShowDetailResponse.VenueInfo(
                venueEntity.getId(),
                venueEntity.getName(),
                venueEntity.getAddress(),
                venueEntity.getRegion(),
                venueEntity.getLatitude(),
                venueEntity.getLongitude(),
                venueEntity.getPhone(),
                venueEntity.getImageUrl())
                : null;

        final ShowDetailResponse response = new ShowDetailResponse(
                showEntity.getId(),
                showEntity.getTitle(),
                showEntity.getSubTitle(),
                showEntity.getInfo(),
                showEntity.getStartDate(),
                showEntity.getEndDate(),
                showEntity.getRunningMinutes(),
                showEntity.getViewCount(),
                likeCount,
                showBookingStatus,
                showEntity.getSaleType(),
                showEntity.getSaleStartDate(),
                showEntity.getSaleEndDate(),
                showEntity.getImage(),
                venueInfo,
                performerInfo,
                genreNames,
                grades,
                performanceDates
        );
        return Optional.of(response);
    }
}
