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
import com.ticket.core.enums.PerformanceSeatState;
import com.ticket.core.enums.Role;

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

    public static Member createMember() {
        int count = EMAIL_COUNTER.incrementAndGet();
        return FIXTURE.giveMeBuilder(Member.class)
                .setNull("id")
                .set(javaGetter(Member::getEmail), Email.create("test" + count + "@example.com"))
                .set(javaGetter(Member::getEncodedPassword), EncodedPassword.create("encoded_password"))
                .set(javaGetter(Member::getName), "테스트유저" + count)
                .set(javaGetter(Member::getRole), Role.MEMBER)
                .sample();
    }

    public static List<Member> createMembers(int count) {
        return IntStream.range(0, count)
                .mapToObj(_ -> createMember())
                .toList();
    }

    public static Performance createPerformance(Show show) {
        return FIXTURE.giveMeBuilder(Performance.class)
                .setNull("id")
                .set(javaGetter(Performance::getShow), show)
                .set(javaGetter(Performance::getPerformanceNo), 1L)
                .set(javaGetter(Performance::getStartTime), BASE_TIME.plusDays(7))
                .set(javaGetter(Performance::getEndTime), BASE_TIME.plusDays(7).plusHours(2))
                .set(javaGetter(Performance::getOrderOpenTime), BASE_TIME.minusDays(1))
                .set(javaGetter(Performance::getOrderCloseTime), BASE_TIME.plusDays(6))
                .set(javaGetter(Performance::getMaxCanHoldCount), 4)
                .set(javaGetter(Performance::getHoldTime), 300)
                .sample();
    }

    public static PerformanceSeat createAvailableSeat(Performance performance, Seat seat) {
        return FIXTURE.giveMeBuilder(PerformanceSeat.class)
                .setNull("id")
                .set(javaGetter(PerformanceSeat::getPerformance), performance)
                .set(javaGetter(PerformanceSeat::getSeat), seat)
                .set(javaGetter(PerformanceSeat::getState), PerformanceSeatState.AVAILABLE)
                .set(javaGetter(PerformanceSeat::getPrice), BigDecimal.valueOf(50000))
                .sample();
    }

    public static List<PerformanceSeat> createAvailableSeats(Performance performance, List<Seat> seats) {
        return seats.stream()
                .map(seat -> createAvailableSeat(performance, seat))
                .toList();
    }

    public static Seat createSeat() {
        return FIXTURE.giveMeBuilder(Seat.class)
                .setNull("id")
                .set(javaGetter(Seat::getRowNo), "row")
                .set(javaGetter(Seat::getSeatNo), "col")
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
                .set(javaGetter(Show::getTitle), "테스트 공연")
                .sample();
    }
}