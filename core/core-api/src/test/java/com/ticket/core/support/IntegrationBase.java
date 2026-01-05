package com.ticket.core.support;

import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberRepository;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceRepository;
import com.ticket.core.domain.performanceseat.PerformanceSeat;
import com.ticket.core.domain.performanceseat.PerformanceSeatRepository;
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
    protected PerformanceSeatRepository performanceSeatRepository;

    protected Performance savedPerformance;
    protected List<Member> savedMembers;
    protected List<PerformanceSeat> savedPerformanceSeats;

    /**
     * 하위 클래스에서 오버라이드 가능
     */
    protected List<Long> getSeatIds() {
        return List.of(1L ,2L, 3L);
    }

    @BeforeAll
    void setUpBase() {
        savedPerformance = performanceRepository.save(TestDataFactory.createPerformance());
        savedPerformanceSeats = performanceSeatRepository.saveAll(
                TestDataFactory.createAvailableSeats(savedPerformance.getId(), getSeatIds())
        );
        savedMembers = memberRepository.saveAll(TestDataFactory.createMembers(100));
    }

    @AfterEach
    void tearDownBase() {
        resetSeatsToAvailable();
    }

    /**
     * 좌석 상태만 리셋 (전체 삭제 없이)
     */
    protected void resetSeatsToAvailable() {
        List<Long> seatIds = savedPerformanceSeats.stream()
                .map(PerformanceSeat::getSeatId)
                .toList();
        performanceSeatRepository.deleteAllInBatch();
        savedPerformanceSeats = performanceSeatRepository.saveAll(
                TestDataFactory.createAvailableSeats(savedPerformance.getId(), seatIds)
        );
    }
}
