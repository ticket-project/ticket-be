package com.ticket.core.domain.support;

import com.ticket.core.config.JpaAuditingConfig;
import com.ticket.core.config.QuerydslConfig;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.vo.Email;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performanceseat.model.PerformanceSeat;
import com.ticket.core.domain.seat.Seat;
import com.ticket.core.domain.show.Show;
import com.ticket.core.domain.show.category.Category;
import com.ticket.core.domain.show.genre.Genre;
import com.ticket.core.domain.show.mapping.ShowGenre;
import com.ticket.core.domain.show.mapping.ShowGrade;
import com.ticket.core.domain.show.mapping.ShowSeat;
import com.ticket.core.domain.show.meta.Region;
import com.ticket.core.domain.show.meta.SaleType;
import com.ticket.core.domain.show.performer.Performer;
import com.ticket.core.domain.show.query.ShowCursorSupport;
import com.ticket.core.domain.show.query.ShowQueryHelper;
import com.ticket.core.domain.show.query.ShowSortSupport;
import com.ticket.core.domain.show.venue.Venue;
import com.ticket.core.domain.showlike.ShowLike;
import com.ticket.core.enums.PerformanceSeatState;
import com.ticket.core.enums.Role;
import com.ticket.core.support.cursor.CursorCodec;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = QueryRepositoryTestSupport.TestApplication.class)
@TestPropertySource(properties = {
        "spring.profiles.active=test",
        "spring.datasource.url=jdbc:h2:mem:query-repository-test;MODE=Oracle;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration,"
                + "org.springframework.boot.data.redis.autoconfigure.DataRedisRepositoriesAutoConfiguration,"
                + "org.redisson.spring.starter.RedissonAutoConfigurationV2,"
                + "org.redisson.spring.starter.RedissonAutoConfigurationV4"
})
@Transactional
@Import({
        JpaAuditingConfig.class,
        QuerydslConfig.class,
        QueryRepositoryTestSupport.TestConfig.class,
        ShowQueryHelper.class,
        ShowSortSupport.class,
        ShowCursorSupport.class
})
@SuppressWarnings("NonAsciiCharacters")
public abstract class QueryRepositoryTestSupport {

    @Autowired
    protected EntityManager entityManager;

    protected Venue persistVenue(final String name, final Region region) throws Exception {
        Venue venue = instantiate(Venue.class);
        ReflectionTestUtils.setField(venue, "name", name);
        ReflectionTestUtils.setField(venue, "address", name + " 주소");
        ReflectionTestUtils.setField(venue, "region", region);
        ReflectionTestUtils.setField(venue, "addressDetail", "상세");
        ReflectionTestUtils.setField(venue, "zipCode", "12345");
        ReflectionTestUtils.setField(venue, "latitude", BigDecimal.valueOf(37.5));
        ReflectionTestUtils.setField(venue, "longitude", BigDecimal.valueOf(127.0));
        ReflectionTestUtils.setField(venue, "phone", "02-0000-0000");
        ReflectionTestUtils.setField(venue, "imageUrl", "https://example.com/venue.png");
        ReflectionTestUtils.setField(venue, "viewBoxWidth", 1000);
        ReflectionTestUtils.setField(venue, "viewBoxHeight", 800);
        ReflectionTestUtils.setField(venue, "seatDiameter", 12.0);
        ReflectionTestUtils.setField(venue, "gapX", 2.0);
        ReflectionTestUtils.setField(venue, "gapY", 2.0);
        entityManager.persist(venue);
        return venue;
    }

    protected Performer persistPerformer(final String name) throws Exception {
        Performer performer = instantiate(Performer.class);
        ReflectionTestUtils.setField(performer, "name", name);
        ReflectionTestUtils.setField(performer, "profileImageUrl", "https://example.com/performer.png");
        entityManager.persist(performer);
        return performer;
    }

    protected Category persistCategory(final String code, final String name) throws Exception {
        Category category = instantiate(Category.class);
        ReflectionTestUtils.setField(category, "code", code);
        ReflectionTestUtils.setField(category, "name", name);
        entityManager.persist(category);
        return category;
    }

    protected Genre persistGenre(final String code, final String name, final Category category) {
        Genre genre = new Genre(code, name, category);
        entityManager.persist(genre);
        return genre;
    }

    protected Show persistShow(
            final String title,
            final Venue venue,
            final Performer performer,
            final long viewCount,
            final LocalDateTime saleStartDate,
            final LocalDateTime saleEndDate
    ) {
        Show show = new Show(
                title,
                title + " 부제",
                title + " 소개",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(30),
                viewCount,
                SaleType.GENERAL,
                saleStartDate,
                saleEndDate,
                "https://example.com/show.png",
                venue,
                performer,
                120
        );
        entityManager.persist(show);
        return show;
    }

    protected ShowGenre persistShowGenre(final Show show, final Genre genre) {
        ShowGenre showGenre = new ShowGenre(show, genre);
        entityManager.persist(showGenre);
        return showGenre;
    }

    protected ShowGrade persistShowGrade(final Show show, final String gradeCode, final String gradeName, final BigDecimal price, final int sortOrder)
            throws Exception {
        ShowGrade showGrade = instantiate(ShowGrade.class);
        ReflectionTestUtils.setField(showGrade, "show", show);
        ReflectionTestUtils.setField(showGrade, "gradeCode", gradeCode);
        ReflectionTestUtils.setField(showGrade, "gradeName", gradeName);
        ReflectionTestUtils.setField(showGrade, "price", price);
        ReflectionTestUtils.setField(showGrade, "sortOrder", sortOrder);
        entityManager.persist(showGrade);
        return showGrade;
    }

    protected Seat persistSeat(final String section, final String rowNo, final String seatNo, final int floor) {
        Seat seat = new Seat(section, rowNo, seatNo, floor, 10.0, 20.0);
        entityManager.persist(seat);
        return seat;
    }

    protected ShowSeat persistShowSeat(final Show show, final Seat seat, final ShowGrade showGrade) throws Exception {
        ShowSeat showSeat = instantiate(ShowSeat.class);
        ReflectionTestUtils.setField(showSeat, "show", show);
        ReflectionTestUtils.setField(showSeat, "seat", seat);
        ReflectionTestUtils.setField(showSeat, "showGrade", showGrade);
        entityManager.persist(showSeat);
        return showSeat;
    }

    protected Performance persistPerformance(final Show show, final long performanceNo, final LocalDateTime startTime) {
        Performance performance = new Performance(
                show,
                performanceNo,
                startTime,
                startTime.plusHours(2),
                startTime.minusDays(10),
                startTime.plusDays(1),
                4,
                300
        );
        entityManager.persist(performance);
        return performance;
    }

    protected PerformanceSeat persistPerformanceSeat(
            final Performance performance,
            final Seat seat,
            final PerformanceSeatState state,
            final BigDecimal price
    ) {
        PerformanceSeat performanceSeat = new PerformanceSeat(performance, seat, state, price);
        entityManager.persist(performanceSeat);
        return performanceSeat;
    }

    protected Member persistMember(final String email, final String name) {
        Member member = Member.createSocialMember(Email.create(email), name, Role.MEMBER);
        entityManager.persist(member);
        return member;
    }

    protected ShowLike persistShowLike(final Member member, final Show show) {
        ShowLike showLike = new ShowLike(member, show);
        entityManager.persist(showLike);
        return showLike;
    }

    protected void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private <T> T instantiate(final Class<T> type) throws Exception {
        Constructor<T> constructor = type.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    static class TestConfig {
        @Bean
        CursorCodec cursorCodec() {
            return new CursorCodec(JsonMapper.builder().build());
        }
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackages = "com.ticket.core.domain")
    @Import(TestConfig.class)
    static class TestApplication {
    }
}
