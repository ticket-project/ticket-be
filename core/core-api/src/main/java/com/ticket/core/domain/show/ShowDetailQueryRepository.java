package com.ticket.core.domain.show;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticket.core.api.controller.response.ShowDetailResponse;
import com.ticket.core.api.controller.response.ShowDetailResponse.GradeInfo;
import com.ticket.core.api.controller.response.ShowDetailResponse.PerformanceDateInfo;
import com.ticket.core.api.controller.response.ShowDetailResponse.PerformanceInfo;
import com.ticket.core.api.controller.response.ShowDetailResponse.PerformerInfo;
import com.ticket.core.domain.performance.Performance;
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

/**
 * Show 상세 조회 전용 Repository
 */
@Repository
public class ShowDetailQueryRepository {

    private final JPAQueryFactory queryFactory;

    public ShowDetailQueryRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 공연 상세 정보 조회
     * - Show 기본정보 + Performer + 장르 목록 + 등급/가격 + 회차
     */
    public Optional<ShowDetailResponse> findShowDetail(Long showId) {
        // 1. Show + Performer 조회
        Show showEntity = queryFactory
                .selectFrom(show)
                .leftJoin(show.performer, performer).fetchJoin()
                .leftJoin(show.venue).fetchJoin()
                .where(show.id.eq(showId))
                .fetchOne();

        if (showEntity == null) {
            return Optional.empty();
        }

        // 2. 장르 목록 조회
        List<String> genreNames = queryFactory
                .select(genre.name)
                .from(showGenre)
                .join(showGenre.genre, genre)
                .where(showGenre.show.id.eq(showId))
                .fetch();

        // 3. 등급/가격 조회
        List<ShowGrade> gradeEntities = queryFactory
                .selectFrom(showGrade)
                .where(showGrade.show.id.eq(showId))
                .orderBy(showGrade.sortOrder.asc())
                .fetch();

        List<GradeInfo> grades = gradeEntities.stream()
                .map(g -> new GradeInfo(g.getId(), g.getGradeCode(), g.getGradeName(), g.getPrice(), g.getSortOrder()))
                .toList();

        // 4. 회차 조회
        List<Performance> performanceEntities = queryFactory
                .selectFrom(performance)
                .where(performance.show.id.eq(showId))
                .orderBy(performance.performanceNo.asc())
                .fetch();

        LocalDateTime now = LocalDateTime.now();
        List<PerformanceInfo> performances = performanceEntities.stream()
                .map(p -> new PerformanceInfo(
                        p.getId(), p.getPerformanceNo(), p.getStartTime(), p.getEndTime(),
                        p.getOrderOpenTime(), p.getOrderCloseTime(), p.getState(), p.calculateBookingStatus(now)))
                .toList();

        List<PerformanceDateInfo> performanceDates = performances.stream()
                .collect(Collectors.groupingBy(
                        performanceInfo -> performanceInfo.startTime().toLocalDate(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ))
                .entrySet().stream()
                .map(entry -> new PerformanceDateInfo(entry.getKey(), entry.getValue()))
                .toList();

        // 5. Performer 정보 매핑
        Performer performerEntity = showEntity.getPerformer();
        PerformerInfo performerInfo = performerEntity != null
                ? new PerformerInfo(performerEntity.getId(), performerEntity.getName(), performerEntity.getProfileImageUrl())
                : null;

        Venue venueEntity = showEntity.getVenue();
        ShowDetailResponse.VenueInfo venueInfo = venueEntity != null
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

        ShowDetailResponse response = new ShowDetailResponse(
                showEntity.getId(),
                showEntity.getTitle(),
                showEntity.getSubTitle(),
                showEntity.getInfo(),
                showEntity.getStartDate(),
                showEntity.getEndDate(),
                showEntity.getRunningMinutes(),
                showEntity.getViewCount(),
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
