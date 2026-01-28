package com.ticket.core.support;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.vo.Email;
import com.ticket.core.domain.member.vo.EncodedPassword;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performanceseat.PerformanceSeat;
import com.ticket.core.domain.seat.Seat;
import com.ticket.core.domain.show.Show;
import com.ticket.core.enums.EntityStatus;
import com.ticket.core.enums.PerformanceSeatState;
import com.ticket.core.enums.PerformanceState;
import com.ticket.core.enums.Role;
import com.ticket.core.domain.show.Region;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static com.navercorp.fixturemonkey.api.expression.JavaGetterMethodPropertySelector.javaGetter;

public class TestDataFactory {
    private static final AtomicInteger EMAIL_COUNTER = new AtomicInteger(0);

    public static final FixtureMonkey FIXTURE = FixtureMonkey.builder()
            .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
            .plugin(new JakartaValidationPlugin())
            .build();

    public static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 10, 10, 0, 0);

    // ===== Member =====
    /**
     * 기본 Member 생성 (유효한 이메일 + MEMBER 역할)
     */
    public static Member createMember() {
        int count = EMAIL_COUNTER.incrementAndGet();
        return FIXTURE.giveMeBuilder(Member.class)
                .setNull("id")
                .set("status", EntityStatus.ACTIVE)
                .set(javaGetter(Member::getEmail), Email.create("test" + count + "@example.com"))
                .set(javaGetter(Member::getEncodedPassword), EncodedPassword.create("encoded_password"))
                .set(javaGetter(Member::getName), "테스트유저" + count)
                .set(javaGetter(Member::getRole), Role.MEMBER)
                .sample();
    }

    /**
     * 여러 Member 생성 (동시성 테스트용)
     * EMAIL_COUNTER를 사용하여 고유한 이메일 생성
     */
    public static List<Member> createMembers(int count) {
        return IntStream.range(0, count)
                .mapToObj(_ -> createMember())
                .toList();
    }

    // ===== Performance 생성 =====

    /**
     * 기본 Performance 생성 (OPEN 상태)
     */
    public static Performance createPerformance(Show show) {
        return createPerformance(show, PerformanceState.OPEN);
    }

    /**
     * 특정 상태의 Performance 생성
     */
    public static Performance createPerformance(Show show, PerformanceState state) {
        return FIXTURE.giveMeBuilder(Performance.class)
                .setNull("id")
                .set("status", EntityStatus.ACTIVE)
                .set(javaGetter(Performance::getShow), show)
                .set(javaGetter(Performance::getRoundNo), 1L)
                .set(javaGetter(Performance::getStartTime), BASE_TIME.plusDays(7))
                .set(javaGetter(Performance::getEndTime), BASE_TIME.plusDays(7).plusHours(2))
                .set(javaGetter(Performance::getOrderOpenTime), BASE_TIME.minusDays(1))
                .set(javaGetter(Performance::getOrderCloseTime), BASE_TIME.plusDays(6))
                .set(javaGetter(Performance::getMaxCanHoldCount), 4)
                .set(javaGetter(Performance::getHoldTime), 300)
                .set(javaGetter(Performance::getState), state)
                .sample();
    }

    // ===== PerformanceSeat 생성 =====

    /**
     * 사용 가능한 회차_좌석 생성
     */
    public static PerformanceSeat createAvailableSeat(Performance performance, Seat seat) {
        return FIXTURE.giveMeBuilder(PerformanceSeat.class)
                .setNull("id")
                .set("status", EntityStatus.ACTIVE)
                .set(javaGetter(PerformanceSeat::getPerformance), performance)
                .set(javaGetter(PerformanceSeat::getSeat), seat)
                .set(javaGetter(PerformanceSeat::getState), PerformanceSeatState.AVAILABLE)
                .set(javaGetter(PerformanceSeat::getPrice), BigDecimal.valueOf(50000))
                .sample();
    }

    /**
     * 여러 사용 가능한 회차_좌석 생성
     */
    public static List<PerformanceSeat> createAvailableSeats(Performance performance, List<Seat> seats) {
        return seats.stream()
                .map(seat -> createAvailableSeat(performance, seat))
                .toList();
    }


    public static Seat createSeat() {
        return FIXTURE.giveMeBuilder(Seat.class)
                .setNull("id")
                .set("status", EntityStatus.ACTIVE)
                .set(javaGetter(Seat::getSeatRow), "row")
                .set(javaGetter(Seat::getSeatCol), "col")
                .sample();
    }

    public static List<Seat> createSeats(int count) {
        return IntStream.range(0, count)
                .mapToObj(_ -> createSeat())
                .toList();
    }

    public static Show createShow() {
        return FIXTURE.giveMeBuilder(Show.class)
                .setNull("id")
                .set("status", EntityStatus.ACTIVE)
                .set(javaGetter(Show::getRegion), Region.SEOUL)
                .set(javaGetter(Show::getTitle), "만약에 우리")
                .sample();
    }
}
