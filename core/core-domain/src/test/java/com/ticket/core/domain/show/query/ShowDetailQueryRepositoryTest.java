package com.ticket.core.domain.show.query;

import com.ticket.core.domain.show.BookingStatus;
import com.ticket.core.domain.show.image.ShowCardImagePathConverter;
import com.ticket.core.domain.show.meta.Region;
import com.ticket.core.domain.show.model.Category;
import com.ticket.core.domain.show.model.Genre;
import com.ticket.core.domain.show.model.Show;
import com.ticket.core.domain.show.performer.Performer;
import com.ticket.core.domain.show.venue.Venue;
import com.ticket.core.domain.support.QueryRepositoryTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Import({
        ShowDetailQueryRepository.class,
        ShowCardImagePathConverter.class
})
@SuppressWarnings("NonAsciiCharacters")
class ShowDetailQueryRepositoryTest extends QueryRepositoryTestSupport {

    @Autowired
    private ShowDetailQueryRepository showDetailQueryRepository;

    private Long showId;

    @BeforeEach
    void setUp() throws Exception {
        Venue venue = persistVenue("예술의전당", Region.SEOUL);
        Performer performer = persistPerformer("홍길동");
        Category category = persistCategory("CONCERT", "콘서트");
        Genre genre = persistGenre("KPOP", "케이팝", category);
        Show show = persistShow(
                "단독 공연",
                venue,
                performer,
                321L,
                LocalDateTime.of(2026, 3, 10, 0, 0),
                LocalDateTime.of(2026, 3, 20, 23, 59)
        );
        showId = show.getId();
        entityManager.createNativeQuery("update shows set image = :image where id = :id")
                .setParameter("image", "/api/images/shows/" + showId + ".png")
                .setParameter("id", showId)
                .executeUpdate();
        persistShowGenre(show, genre);
        persistShowGrade(show, "VIP", "VIP석", BigDecimal.valueOf(150000), 1);
        persistShowGrade(show, "R", "R석", BigDecimal.valueOf(100000), 2);
        persistPerformance(show, 1L, LocalDate.of(2026, 3, 16).atTime(14, 0));
        persistPerformance(show, 2L, LocalDate.of(2026, 3, 16).atTime(19, 0));
        persistShowLike(persistMember("a@example.com", "A"), show);
        persistShowLike(persistMember("b@example.com", "B"), show);
        flushAndClear();
    }

    @Test
    void 공연_상세정보를_조합해_조회하고_예매상태는_Clock_기준으로_계산한다() {
        Optional<GetShowDetailUseCase.Output> result = showDetailQueryRepository.findShowDetail(showId);

        assertThat(result).isPresent();
        GetShowDetailUseCase.Output detail = result.orElseThrow();
        assertThat(detail.title()).isEqualTo("단독 공연");
        assertThat(detail.likeCount()).isEqualTo(2L);
        assertThat(detail.genreNames()).contains("케이팝");
        assertThat(detail.grades()).extracting(GetShowDetailUseCase.GradeInfo::gradeCode).containsExactly("VIP", "R");
        assertThat(detail.performanceDates()).hasSize(1);
        assertThat(detail.performanceDates().getFirst().performances()).hasSize(2);
        assertThat(detail.image()).isEqualTo("/api/images/shows/card/" + showId + ".jpg");
        assertThat(detail.venue().name()).isEqualTo("예술의전당");
        assertThat(detail.performer().name()).isEqualTo("홍길동");
        assertThat(detail.bookingStatus()).isEqualTo(BookingStatus.ON_SALE);
    }

    @Test
    void 존재하지_않는_공연이면_empty를_반환한다() {
        assertThat(showDetailQueryRepository.findShowDetail(999999L)).isEmpty();
    }
}
