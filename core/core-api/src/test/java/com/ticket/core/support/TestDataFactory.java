package com.ticket.core.support;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.vo.Email;
import com.ticket.core.domain.member.vo.EncodedPassword;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performanceseat.PerformanceSeat;
import com.ticket.core.enums.EntityStatus;
import com.ticket.core.enums.PerformanceSeatState;
import com.ticket.core.enums.PerformanceState;
import com.ticket.core.enums.Role;

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

    public static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 1, 1, 0, 0);

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
     * 커스텀 이메일로 Member 생성
     */
    public static Member createMember(String email) {
        return FIXTURE.giveMeBuilder(Member.class)
                .setNull("id")
                .set("status", EntityStatus.ACTIVE)
                .set(javaGetter(Member::getEmail), Email.create(email))
                .set(javaGetter(Member::getEncodedPassword), EncodedPassword.create("encoded_password"))
                .set(javaGetter(Member::getName), "테스트유저")
                .set(javaGetter(Member::getRole), Role.MEMBER)
                .sample();
    }

    /**
     * 여러 Member 생성 (동시성 테스트용)
     * EMAIL_COUNTER를 사용하여 고유한 이메일 생성
     */
    public static List<Member> createMembers(int count) {
        int startIdx = EMAIL_COUNTER.get(); // 현재 카운터 값
        return IntStream.range(0, count)
                .mapToObj(i -> createMember("user" + (startIdx + i) + "_" + System.nanoTime() + "@test.com"))
                .toList();
    }

    // ===== Performance 생성 =====

    /**
     * 기본 Performance 생성 (OPEN 상태)
     */
    public static Performance createPerformance() {
        return createPerformance(PerformanceState.OPEN);
    }

    /**
     * 특정 상태의 Performance 생성
     */
    public static Performance createPerformance(PerformanceState state) {
        LocalDateTime baseTime = LocalDateTime.now();
        return FIXTURE.giveMeBuilder(Performance.class)
                .setNull("id")
                .set("status", EntityStatus.ACTIVE)
                .set(javaGetter(Performance::getShowId), 1L)
                .set(javaGetter(Performance::getRoundNo), 1L)
                .set(javaGetter(Performance::getStartTime), baseTime.plusDays(7))
                .set(javaGetter(Performance::getEndTime), baseTime.plusDays(7).plusHours(2))
                .set(javaGetter(Performance::getReserveOpenTime), baseTime.minusDays(1))
                .set(javaGetter(Performance::getReserveCloseTime), baseTime.plusDays(6))
                .set(javaGetter(Performance::getMaxCanReserveCount), 4)
                .set(javaGetter(Performance::getHoldTime), 300)
                .set(javaGetter(Performance::getState), state)
                .sample();
    }

    // ===== PerformanceSeat 생성 =====

    /**
     * 사용 가능한 좌석 생성
     */
    public static PerformanceSeat createAvailableSeat(Long performanceId, Long seatId) {
        return FIXTURE.giveMeBuilder(PerformanceSeat.class)
                .setNull("id")
                .set("status", EntityStatus.ACTIVE)
                .set(javaGetter(PerformanceSeat::getPerformanceId), performanceId)
                .set(javaGetter(PerformanceSeat::getSeatId), seatId)
                .set(javaGetter(PerformanceSeat::getState), PerformanceSeatState.AVAILABLE)
                .setNull(javaGetter(PerformanceSeat::getHoldExpireAt))
                .setNull(javaGetter(PerformanceSeat::getHoldByMemberId))
                .setNull(javaGetter(PerformanceSeat::getHoldToken))
                .sample();
    }

    /**
     * 여러 사용 가능한 좌석 생성
     */
    public static List<PerformanceSeat> createAvailableSeats(Long performanceId, List<Long> seatIds) {
        return seatIds.stream()
                .map(seatId -> createAvailableSeat(performanceId, seatId))
                .toList();
    }

    /**
     * 선점된 좌석 생성
     */
    public static PerformanceSeat createHeldSeat(Long performanceId, Long seatId, Long memberId) {
        return FIXTURE.giveMeBuilder(PerformanceSeat.class)
                .setNull("id")
                .set(javaGetter(PerformanceSeat::getPerformanceId), performanceId)
                .set(javaGetter(PerformanceSeat::getSeatId), seatId)
                .set(javaGetter(PerformanceSeat::getState), PerformanceSeatState.HELD)
                .set(javaGetter(PerformanceSeat::getHoldExpireAt), LocalDateTime.now().plusMinutes(5))
                .set(javaGetter(PerformanceSeat::getHoldByMemberId), memberId)
                .set(javaGetter(PerformanceSeat::getHoldToken), "test-token")
                .sample();
    }
}
