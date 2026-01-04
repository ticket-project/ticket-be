package com.ticket.core.domain.reservation;

import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberRepository;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceRepository;
import com.ticket.core.domain.performanceseat.PerformanceSeat;
import com.ticket.core.domain.performanceseat.PerformanceSeatRepository;
import com.ticket.core.enums.PerformanceSeatState;
import com.ticket.core.support.IntegrationBase;
import com.ticket.core.support.TestDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
class ReservationServiceTest extends IntegrationBase {

    @Autowired
    @Qualifier("reservationServiceV1")
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

    private Member savedMember;
    private Performance savedPerformance;
    private List<PerformanceSeat> savedPerformanceSeats;

    @BeforeEach
    void setUp() {
        savedMember = memberRepository.save(TestDataFactory.createMember());
        savedPerformance = performanceRepository.save(TestDataFactory.createPerformance());
        savedPerformanceSeats = performanceSeatRepository.saveAll(
                TestDataFactory.createAvailableSeats(savedPerformance.getId(), List.of(1L))
        );
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
    void 예매를_성공한다() {
        // given
        final NewReservation newReservation = new NewReservation(
                savedMember.getId(),
                savedPerformance.getId(),
                List.of(1L)
        );
        // when
        reservationService.addReservation(newReservation);
        final List<PerformanceSeat> reservedSeats = performanceSeatRepository.findAllById(
                savedPerformanceSeats.stream().map(PerformanceSeat::getId).toList()
        );
        // then
        assertThat(reservedSeats).allMatch(seat -> seat.getState() == PerformanceSeatState.RESERVED);
        final List<Reservation> reservations = reservationRepository.findAll();
        assertThat(reservations).hasSize(1);
        assertThat(reservations.getFirst().getMemberId()).isEqualTo(savedMember.getId());

        final List<ReservationDetail> details = reservationDetailRepository.findAll();
        assertThat(details).hasSize(1);
    }
}
