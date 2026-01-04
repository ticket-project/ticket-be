package com.ticket.core.domain.reservation;

import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberRepository;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceRepository;
import com.ticket.core.domain.performanceseat.PerformanceSeat;
import com.ticket.core.domain.performanceseat.PerformanceSeatRepository;
import com.ticket.core.support.IntegrationBase;
import com.ticket.core.support.TestDataFactory;
import com.ticket.util.ConcurrentTestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
class ReservationServiceV0ConcurrencyTest extends IntegrationBase {

    @Autowired
    @Qualifier("reservationServiceV0")
    private ReservationService reservationService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PerformanceRepository performanceRepository;
    @Autowired
    private PerformanceSeatRepository performanceSeatRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationDetailRepository reservationDetailRepository;

    private List<Member> savedMembers;
    private Performance savedPerformance;
    private List<PerformanceSeat> savedPerformanceSeats;

    @BeforeEach
    void setUp() {
        savedPerformance = performanceRepository.save(TestDataFactory.createPerformance());
        savedPerformanceSeats = performanceSeatRepository.saveAll(
                TestDataFactory.createAvailableSeats(savedPerformance.getId(), List.of(1L))
        );
        savedMembers = memberRepository.saveAll(TestDataFactory.createMembers(100));
    }

    @AfterEach
    void tearDown() {
        reservationDetailRepository.deleteAllInBatch();
        reservationRepository.deleteAllInBatch();
        performanceSeatRepository.deleteAllInBatch();
        performanceRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Test
    void 재고가_1개일때_여러_요청이_동시에_들어오면_db_update_잠금으로_인해_예매가_오버셀되지_않는다() throws InterruptedException {
        // given & when & then
        ConcurrentTestUtil.execute(100, idx -> reservationService.addReservation(new NewReservation(
                savedMembers.get(idx).getId(),
                savedPerformance.getId(),
                savedPerformanceSeats.stream().map(PerformanceSeat::getSeatId).toList()
        )));
        final long reservationCount = reservationRepository.count();
        assertThat(reservationCount).isEqualTo(1);
    }
}
