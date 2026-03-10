package com.ticket.core.domain.show;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticket.core.api.controller.response.ShowDetailResponse;
import com.ticket.core.api.controller.response.ShowDetailResponse.GradeInfo;
import com.ticket.core.api.controller.response.ShowDetailResponse.PerformanceDateInfo;
import com.ticket.core.api.controller.response.ShowDetailResponse.PerformanceInfo;
import com.ticket.core.api.controller.response.ShowDetailResponse.PerformerInfo;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.enums.SaleStatus;
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
        final Show showEntity = fetchShow(showId);
        if (showEntity == null) {
            return Optional.empty();
        }

        final List<String> genreNames = fetchGenreNames(showId);
        final List<GradeInfo> grades = fetchGrades(showId);
        final List<PerformanceDateInfo> performanceDates = fetchPerformanceDates(showId);
        final long likeCount = fetchLikeCount(showId);

        return Optional.of(toShowDetailResponse(showEntity, genreNames, grades, performanceDates, likeCount));
    }

    private Show fetchShow(final Long showId) {
        return queryFactory
                .selectFrom(show)
                .leftJoin(show.performer, performer).fetchJoin()
                .leftJoin(show.venue).fetchJoin()
                .where(show.id.eq(showId))
                .fetchOne();
    }

    private List<String> fetchGenreNames(final Long showId) {
        return queryFactory
                .select(genre.name)
                .from(showGenre)
                .join(showGenre.genre, genre)
                .where(showGenre.show.id.eq(showId))
                .fetch();
    }

    private List<GradeInfo> fetchGrades(final Long showId) {
        return queryFactory
                .selectFrom(showGrade)
                .where(showGrade.show.id.eq(showId))
                .orderBy(showGrade.sortOrder.asc())
                .fetch()
                .stream()
                .map(this::toGradeInfo)
                .toList();
    }

    private GradeInfo toGradeInfo(final ShowGrade grade) {
        return new GradeInfo(
                grade.getId(),
                grade.getGradeCode(),
                grade.getGradeName(),
                grade.getPrice(),
                grade.getSortOrder()
        );
    }

    private List<PerformanceDateInfo> fetchPerformanceDates(final Long showId) {
        final List<PerformanceInfo> performances = fetchPerformances(showId).stream()
                .map(this::toPerformanceInfo)
                .toList();

        return performances.stream()
                .collect(Collectors.groupingBy(
                        performanceInfo -> performanceInfo.startTime().toLocalDate(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ))
                .entrySet().stream()
                .map(entry -> new PerformanceDateInfo(entry.getKey(), entry.getValue()))
                .toList();
    }

    private List<Performance> fetchPerformances(final Long showId) {
        return queryFactory
                .selectFrom(performance)
                .where(performance.show.id.eq(showId))
                .orderBy(performance.startTime.asc(), performance.performanceNo.asc())
                .fetch();
    }

    private PerformanceInfo toPerformanceInfo(final Performance performanceEntity) {
        return new PerformanceInfo(
                performanceEntity.getId(),
                performanceEntity.getPerformanceNo(),
                performanceEntity.getStartTime(),
                performanceEntity.getEndTime(),
                performanceEntity.getOrderOpenTime(),
                performanceEntity.getOrderCloseTime()
        );
    }

    private long fetchLikeCount(final Long showId) {
        return Optional.ofNullable(
                queryFactory.select(showLike.count())
                        .from(showLike)
                        .where(showLike.show.id.eq(showId))
                        .fetchOne()
        ).orElse(0L);
    }

    private ShowDetailResponse toShowDetailResponse(
            final Show showEntity,
            final List<String> genreNames,
            final List<GradeInfo> grades,
            final List<PerformanceDateInfo> performanceDates,
            final long likeCount
    ) {
        final SaleStatus saleStatus = showEntity.getSaleStatus(LocalDateTime.now());

        return new ShowDetailResponse(
                showEntity.getId(),
                showEntity.getTitle(),
                showEntity.getSubTitle(),
                showEntity.getInfo(),
                showEntity.getStartDate(),
                showEntity.getEndDate(),
                showEntity.getRunningMinutes(),
                showEntity.getViewCount(),
                likeCount,
                saleStatus,
                showEntity.getSaleType(),
                showEntity.getSaleStartDate(),
                showEntity.getSaleEndDate(),
                showEntity.getImage(),
                toVenueInfo(showEntity.getVenue()),
                toPerformerInfo(showEntity.getPerformer()),
                genreNames,
                grades,
                performanceDates
        );
    }

    private PerformerInfo toPerformerInfo(final Performer performerEntity) {
        if (performerEntity == null) {
            return null;
        }
        return new PerformerInfo(
                performerEntity.getId(),
                performerEntity.getName(),
                performerEntity.getProfileImageUrl()
        );
    }

    private ShowDetailResponse.VenueInfo toVenueInfo(final Venue venueEntity) {
        if (venueEntity == null) {
            return null;
        }
        return new ShowDetailResponse.VenueInfo(
                venueEntity.getId(),
                venueEntity.getName(),
                venueEntity.getAddress(),
                venueEntity.getRegion(),
                venueEntity.getLatitude(),
                venueEntity.getLongitude(),
                venueEntity.getPhone(),
                venueEntity.getImageUrl()
        );
    }
}