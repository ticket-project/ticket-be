package com.ticket.core.domain.hold;

import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberRepository;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceRepository;
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
class HoldServiceV1ConcurrencyTest extends IntegrationBase {

    @Autowired private HoldService holdService;
    @Autowired private MemberRepository memberRepository;
    @Autowired private PerformanceRepository performanceRepository;
    @Autowired private PerformanceSeatRepository performanceSeatRepository;

    private List<Member> savedMembers;
    private Performance savedPerformance;

    @BeforeEach
    void setUp() {
        memberRepository.save(TestDataFactory.createMember());
        savedPerformance = performanceRepository.save(TestDataFactory.createPerformance());
        performanceSeatRepository.saveAll(
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
        ConcurrentTestUtil.execute(100, () -> holdService.hold(new NewSeatHold(
                savedMembers.getFirst().getId(),
                savedPerformance.getId(),
                List.of(1L)
        )));
    }

}