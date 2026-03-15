package com.ticket.core.domain.show.query;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.ticket.core.domain.show.meta.Region;
import com.ticket.core.enums.BookingStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
class ShowQueryHelperTest {

    private final ShowQueryHelper showQueryHelper = new ShowQueryHelper();

    @Test
    void 빈_문자열_필터는_null_조건을_반환한다() {
        //given
        //when
        //then
        assertThat(showQueryHelper.categoryCodeEq(" ")).isNull();
        assertThat(showQueryHelper.genreEq(" ")).isNull();
        assertThat(showQueryHelper.titleContains(" ")).isNull();
        assertThat(showQueryHelper.keywordContains(" ")).isNull();
    }

    @Test
    void 유효한_문자열_필터는_조건식을_반환한다() {
        //given
        //when
        //then
        assertThat(showQueryHelper.categoryCodeEq("CONCERT")).isNotNull();
        assertThat(showQueryHelper.genreEq("KPOP")).isNotNull();
        assertThat(showQueryHelper.titleContains("뮤지컬")).isNotNull();
        assertThat(showQueryHelper.keywordContains("뮤지컬")).isNotNull();
    }

    @Test
    void 지역과_날짜_필터는_null이면_null을_반환하고_값이_있으면_조건을_반환한다() {
        //given
        //when
        //then
        assertThat(showQueryHelper.regionEq(null)).isNull();
        assertThat(showQueryHelper.saleStartDateGoe(null)).isNull();
        assertThat(showQueryHelper.saleStartDateLoe(null)).isNull();
        assertThat(showQueryHelper.saleEndDateGoe(null)).isNull();
        assertThat(showQueryHelper.saleEndDateLoe(null)).isNull();
        assertThat(showQueryHelper.startDateGoe(null)).isNull();
        assertThat(showQueryHelper.startDateLoe(null)).isNull();

        assertThat(showQueryHelper.regionEq(Region.SEOUL)).isNotNull();
        assertThat(showQueryHelper.saleStartDateGoe(LocalDateTime.of(2026, 3, 1, 0, 0))).isNotNull();
        assertThat(showQueryHelper.saleStartDateLoe(LocalDateTime.of(2026, 3, 31, 23, 59))).isNotNull();
        assertThat(showQueryHelper.saleEndDateGoe(LocalDateTime.of(2026, 4, 1, 0, 0))).isNotNull();
        assertThat(showQueryHelper.saleEndDateLoe(LocalDateTime.of(2026, 4, 30, 23, 59))).isNotNull();
        assertThat(showQueryHelper.startDateGoe(LocalDate.of(2026, 3, 1))).isNotNull();
        assertThat(showQueryHelper.startDateLoe(LocalDate.of(2026, 3, 31))).isNotNull();
    }

    @Test
    void bookingStatus가_null이면_null을_반환한다() {
        //given
        //when
        //then
        assertThat(showQueryHelper.bookingStatusCondition(null)).isNull();
    }

    @Test
    void bookingStatus별로_조건식을_반환한다() {
        //given
        //when
        BooleanExpression beforeOpen = showQueryHelper.bookingStatusCondition(BookingStatus.BEFORE_OPEN);
        BooleanExpression onSale = showQueryHelper.bookingStatusCondition(BookingStatus.ON_SALE);
        BooleanExpression closed = showQueryHelper.bookingStatusCondition(BookingStatus.CLOSED);

        //then
        assertThat(beforeOpen).isNotNull();
        assertThat(onSale).isNotNull();
        assertThat(closed).isNotNull();
        assertThat(beforeOpen.toString()).contains("saleStartDate");
        assertThat(onSale.toString()).contains("saleStartDate").contains("saleEndDate");
        assertThat(closed.toString()).contains("saleEndDate");
    }
}

