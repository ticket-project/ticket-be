package com.ticket.core.domain.hold;

import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberRepository;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceRepository;
import com.ticket.core.domain.performanceseat.PerformanceSeat;
import com.ticket.core.domain.performanceseat.PerformanceSeatRepository;
import com.ticket.core.enums.PerformanceSeatState;
import com.ticket.core.enums.Role;
import com.ticket.core.support.IntegrationBase;
import com.ticket.core.support.TestDataFactory;
import com.ticket.util.ConcurrentTestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
class HoldServiceV1ConcurrencyTest extends IntegrationBase {

    @Autowired private HoldService holdService;
    @Autowired private MemberRepository memberRepository;
    @Autowired private PerformanceRepository performanceRepository;
    @Autowired private PerformanceSeatRepository performanceSeatRepository;

    private List<Member> savedMembers;
    private Performance savedPerformance;
    private List<PerformanceSeat> savedPerformanceSeats;

    @BeforeEach
    void setUp() {
        memberRepository.save(TestDataFactory.createMember());
        savedPerformance = performanceRepository.save(TestDataFactory.createPerformance());
        savedPerformanceSeats = performanceSeatRepository.saveAll(
                TestDataFactory.createAvailableSeats(savedPerformance.getId(), List.of(1L))
        );
        savedMembers = IntStream.range(0, 100)
                .mapToObj(i -> memberRepository.save(TestDataFactory.createMember("user" + i + "@test.com", "password", "name", Role.MEMBER)))
                .toList();
    }

    @AfterEach
    void tearDown() {
        performanceSeatRepository.deleteAllInBatch();
        performanceRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Test
    void 동시에_같은_좌석_선점_시도시_분산락에_의해_하나만_성공한다() throws InterruptedException {
        // given & when & then
        ConcurrentTestUtil.execute(100, idx -> holdService.hold(new NewSeatHold(
                savedMembers.get(idx).getId(),
                savedPerformance.getId(),
                savedPerformanceSeats.stream().map(PerformanceSeat::getSeatId).toList()
        )));
    }

    @Test
    void 동시_요청_상황에서_한_요청이_선점_성공_후_좌석_상태가_HELD로_변경된다() throws InterruptedException {
        // given & when
        ConcurrentTestUtil.execute(100, idx -> holdService.hold(new NewSeatHold(
                savedMembers.get(idx).getId(),
                savedPerformance.getId(),
                savedPerformanceSeats.stream().map(PerformanceSeat::getSeatId).toList()
        )));

        // then
        List<PerformanceSeat> seats = performanceSeatRepository.findAllByPerformanceIdAndSeatIdIn(
                savedPerformance.getId(), savedPerformanceSeats.stream().map(PerformanceSeat::getSeatId).toList()
        );

        assertThat(seats).hasSize(1);
        PerformanceSeat seat = seats.getFirst();
        assertThat(seat.getState()).isEqualTo(PerformanceSeatState.HELD);
        assertThat(seat.getHoldToken()).isNotNull();
        assertThat(seat.getHoldByMemberId()).isNotNull();
        assertThat(seat.getHoldExpireAt()).isAfter(LocalDateTime.now());
    }

}