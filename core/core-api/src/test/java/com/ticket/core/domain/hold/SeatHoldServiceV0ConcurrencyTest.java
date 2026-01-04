package com.ticket.core.domain.hold;

import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberRepository;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceRepository;
import com.ticket.core.domain.performanceseat.PerformanceSeat;
import com.ticket.core.domain.performanceseat.PerformanceSeatRepository;
import com.ticket.core.enums.Role;
import com.ticket.core.support.IntegrationBase;
import com.ticket.core.support.TestDataFactory;
import com.ticket.util.ConcurrentTestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.IntStream;

@SuppressWarnings("NonAsciiCharacters")
class SeatHoldServiceV0ConcurrencyTest extends IntegrationBase {

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
    void 재고가_1개일때_여러_요청이_동시에_들어오면_비관적_락에_의해_한_명만_선점한다() throws InterruptedException {
        // given & when & then
        ConcurrentTestUtil.execute(100, idx -> holdService.hold(new NewSeatHold(
                savedMembers.get(idx).getId(),
                savedPerformance.getId(),
                savedPerformanceSeats.stream().map(PerformanceSeat::getSeatId).toList()
        )));
    }

}
