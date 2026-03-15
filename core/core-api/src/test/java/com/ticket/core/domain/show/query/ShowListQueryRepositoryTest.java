package com.ticket.core.domain.show.query;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticket.core.config.JpaAuditingConfig;
import com.ticket.core.config.QuerydslConfig;
import com.ticket.core.api.controller.response.ShowResponse;
import com.ticket.core.api.controller.response.ShowSearchResponse;
import com.ticket.core.domain.show.Show;
import com.ticket.core.domain.show.meta.Region;
import com.ticket.core.domain.show.meta.SaleType;
import com.ticket.core.domain.show.query.model.ShowParam;
import com.ticket.core.domain.show.query.model.ShowSearchRequest;
import com.ticket.core.domain.show.venue.Venue;
import com.ticket.core.enums.BookingStatus;
import com.ticket.core.support.cursor.CursorCodec;
import com.ticket.core.support.cursor.CursorSlice;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.NONE, classes = ShowListQueryRepositoryTest.TestApplication.class)
@TestPropertySource(properties = {
        "spring.profiles.active=test",
        "spring.datasource.url=jdbc:h2:mem:show-query-test;MODE=Oracle;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
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
        ShowListQueryRepositoryTest.TestConfig.class,
        ShowListQueryRepository.class,
        ShowQueryHelper.class,
        ShowSortSupport.class,
        ShowCursorSupport.class
})
@SuppressWarnings("NonAsciiCharacters")
class ShowListQueryRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ShowListQueryRepository showListQueryRepository;

    @Autowired
    private CursorCodec cursorCodec;

    private Venue seoulVenue;
    private Venue busanVenue;

    @BeforeEach
    void setUp() throws Exception {
        seoulVenue = persistVenue("서울 공연장", Region.SEOUL);
        busanVenue = persistVenue("부산 공연장", Region.GYEONGSANG);

        persistShow("서울 인기 공연", 300L, LocalDate.now().plusDays(5), seoulVenue, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(10));
        persistShow("서울 보통 공연", 120L, LocalDate.now().plusDays(10), seoulVenue, LocalDateTime.now().minusDays(2), LocalDateTime.now().plusDays(8));
        persistShow("부산 공연", 999L, LocalDate.now().plusDays(3), busanVenue, LocalDateTime.now().minusDays(3), LocalDateTime.now().plusDays(7));
        persistShow("판매 종료 공연", 50L, LocalDate.now().minusDays(1), seoulVenue, LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(2));

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void 지역으로_필터링하고_인기순으로_공연을_조회한다() {
        ShowParam param = new ShowParam(null, null, Region.SEOUL, null);

        CursorSlice<ShowResponse> result = showListQueryRepository.findAllBySearch(param, 10, "popular");

        Slice<ShowResponse> slice = result.slice();
        assertThat(slice.getContent()).extracting(ShowResponse::title)
                .containsExactly("서울 인기 공연", "서울 보통 공연", "판매 종료 공연");
        assertThat(slice.getContent()).extracting(ShowResponse::viewCount)
                .containsExactly(300L, 120L, 50L);
        assertThat(result.nextCursor()).isNull();
    }

    @Test
    void 인기순_커서를_전달하면_다음_페이지를_조회한다() {
        ShowParam firstPageParam = new ShowParam(null, null, Region.SEOUL, null);
        CursorSlice<ShowResponse> firstPage = showListQueryRepository.findAllBySearch(firstPageParam, 1, "popular");

        ShowParam secondPageParam = new ShowParam(null, null, Region.SEOUL, firstPage.nextCursor());
        CursorSlice<ShowResponse> secondPage = showListQueryRepository.findAllBySearch(secondPageParam, 1, "popular");

        assertThat(firstPage.slice().getContent()).extracting(ShowResponse::title)
                .containsExactly("서울 인기 공연");
        assertThat(firstPage.nextCursor()).isNotBlank();
        assertThat(secondPage.slice().getContent()).extracting(ShowResponse::title)
                .containsExactly("서울 보통 공연");
    }

    @Test
    void 검색_조건에_맞는_공연_수만_집계한다() {
        ShowSearchRequest request = new ShowSearchRequest(
                "서울",
                null,
                BookingStatus.ON_SALE,
                null,
                null,
                Region.SEOUL,
                null
        );

        long count = showListQueryRepository.countSearchShows(request);

        assertThat(count).isEqualTo(2L);
    }

    @Test
    void 검색_API는_예매중인_서울_공연만_조회한다() {
        ShowSearchRequest request = new ShowSearchRequest(
                "서울",
                null,
                BookingStatus.ON_SALE,
                null,
                null,
                Region.SEOUL,
                null
        );

        CursorSlice<ShowSearchResponse> result = showListQueryRepository.searchShows(request, 10, "popular");

        assertThat(result.slice().getContent()).extracting(ShowSearchResponse::title)
                .containsExactly("서울 인기 공연", "서울 보통 공연");
    }

    @Test
    void 조건에_맞는_공연이_없으면_빈_슬라이스를_반환한다() {
        ShowParam param = new ShowParam(null, null, Region.JEOLLA, null);

        CursorSlice<ShowResponse> result = showListQueryRepository.findAllBySearch(param, 10, "popular");

        assertThat(result.slice().getContent()).isEmpty();
        assertThat(result.slice().hasNext()).isFalse();
        assertThat(result.nextCursor()).isNull();
    }

    private Venue persistVenue(final String name, final Region region) throws Exception {
        Constructor<Venue> constructor = Venue.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Venue venue = constructor.newInstance();
        ReflectionTestUtils.setField(venue, "name", name);
        ReflectionTestUtils.setField(venue, "address", name + " 주소");
        ReflectionTestUtils.setField(venue, "region", region);
        ReflectionTestUtils.setField(venue, "addressDetail", "상세 주소");
        ReflectionTestUtils.setField(venue, "zipCode", "12345");
        ReflectionTestUtils.setField(venue, "latitude", BigDecimal.valueOf(37.0));
        ReflectionTestUtils.setField(venue, "longitude", BigDecimal.valueOf(127.0));
        ReflectionTestUtils.setField(venue, "phone", "010-0000-0000");
        ReflectionTestUtils.setField(venue, "imageUrl", "https://example.com/venue.png");
        ReflectionTestUtils.setField(venue, "viewBoxWidth", 1000);
        ReflectionTestUtils.setField(venue, "viewBoxHeight", 800);
        ReflectionTestUtils.setField(venue, "seatDiameter", 10.0);
        ReflectionTestUtils.setField(venue, "gapX", 2.0);
        ReflectionTestUtils.setField(venue, "gapY", 2.0);
        entityManager.persist(venue);
        return venue;
    }

    private Show persistShow(
            final String title,
            final long viewCount,
            final LocalDate startDate,
            final Venue venue,
            final LocalDateTime saleStartDate,
            final LocalDateTime saleEndDate
    ) {
        Show show = new Show(
                title,
                title + " 부제",
                title + " 소개",
                startDate,
                startDate.plusDays(30),
                viewCount,
                SaleType.GENERAL,
                saleStartDate,
                saleEndDate,
                "https://example.com/show.png",
                venue,
                null,
                120
        );
        entityManager.persist(show);
        return show;
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
