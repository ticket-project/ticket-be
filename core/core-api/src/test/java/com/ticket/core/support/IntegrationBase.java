package com.ticket.core.support;

import com.ticket.core.domain.hold.HoldItemRepository;
import com.ticket.core.domain.hold.HoldRepository;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberRepository;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceRepository;
import com.ticket.core.domain.performanceseat.PerformanceSeat;
import com.ticket.core.domain.performanceseat.PerformanceSeatRepository;
import com.ticket.core.domain.seat.Seat;
import com.ticket.core.domain.seat.SeatRepository;
import com.ticket.core.domain.show.Show;
import com.ticket.core.domain.show.ShowJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class IntegrationBase {

    @Autowired
    protected MemberRepository memberRepository;
    @Autowired
    protected PerformanceRepository performanceRepository;
    @Autowired
    protected ShowJpaRepository showJpaRepository;
    @Autowired
    protected SeatRepository seatRepository;
    @Autowired
    protected PerformanceSeatRepository performanceSeatRepository;
    @Autowired
    protected HoldRepository holdRepository;
    @Autowired
    protected HoldItemRepository holdItemRepository;

    protected Show savedShow;
    protected Performance savedPerformance;
    protected List<Seat> savedSeats;
    protected List<Member> savedMembers;
    protected List<PerformanceSeat> savedPerformanceSeats;

    /**
     * 하위 클래스에서 오버라이드 가능
     */

    @BeforeAll
    void setUpBase() {
        savedShow = showJpaRepository.save(TestDataFactory.createShow());
        savedPerformance = performanceRepository.save(TestDataFactory.createPerformance(savedShow));
        savedSeats = seatRepository.saveAll(TestDataFactory.createSeats(5));
        savedPerformanceSeats = performanceSeatRepository.saveAll(
                TestDataFactory.createAvailableSeats(savedPerformance, savedSeats)
        );
        savedMembers = memberRepository.saveAll(TestDataFactory.createMembers(100));
    }

    @AfterEach
    void tearDownBase() {
        clearHoldData();
    }

    /**
     * 선점 상태만 리셋 (전체 삭제 없이)
     */
    protected void clearHoldData() {
        holdItemRepository.deleteAllInBatch();
        holdRepository.deleteAllInBatch();
    }
}
