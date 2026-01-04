package com.ticket.core.domain.reservation;

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

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
class ReservationServiceV1ConcurrencyTest extends IntegrationBase {

    @Autowired
    private ReservationServiceV1 reservationService;
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

    private List<Member> saveMembers;
    private Performance savedPerformance;

    @BeforeEach
    void setUp() {
        memberRepository.save(TestDataFactory.createMember());
        savedPerformance = performanceRepository.save(TestDataFactory.createPerformance());
        performanceSeatRepository.saveAll(
                TestDataFactory.createAvailableSeats(savedPerformance.getId(), List.of(1L))
        );
        saveMembers = IntStream.range(0, 100)
                .mapToObj(i -> memberRepository.save(TestDataFactory.createMember("user" + i + "@test.com", "password", "name", Role.MEMBER)))
                .toList();
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
    void 재고가_1개일때_여러_요청이_동시에_들어오면_비관적_락에_의해_예매가_오버셀되지_않는다() throws InterruptedException {
        // given & when & then
        ConcurrentTestUtil.execute(100, () -> reservationService.addReservation(new NewReservation(
                saveMembers.getFirst().getId(),
                savedPerformance.getId(),
                List.of(1L)
        )));
        final long reservationCount = reservationRepository.count();
        assertThat(reservationCount).isEqualTo(1);
    }

}
