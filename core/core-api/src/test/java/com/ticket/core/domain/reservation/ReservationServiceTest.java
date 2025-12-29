package com.ticket.core.domain.reservation;

import com.ticket.core.enums.PerformanceSeatState;
import com.ticket.core.support.TestDataFactory;
import com.ticket.storage.db.core.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@SuppressWarnings("NonAsciiCharacters")
class ReservationServiceTest {

    @Autowired private ReservationService reservationService;
    @Autowired private MemberRepository memberRepository;
    @Autowired private PerformanceRepository performanceRepository;
    @Autowired private PerformanceSeatRepository performanceSeatRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private ReservationDetailRepository reservationDetailRepository;

    private MemberEntity savedMember;
    private PerformanceEntity savedPerformance;
    private List<PerformanceSeatEntity> savedPerformanceSeats;

    @BeforeEach
    void setUp() {
        savedMember = memberRepository.save(TestDataFactory.createMember());
        savedPerformance = performanceRepository.save(TestDataFactory.createPerformance());
        savedPerformanceSeats = performanceSeatRepository.saveAll(
                TestDataFactory.createAvailableSeats(savedPerformance.getId(), List.of(1L, 2L))
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
                List.of(1L, 2L)
        );
        // when
        reservationService.addReservation(newReservation);
        final List<PerformanceSeatEntity> reservedSeats = performanceSeatRepository.findAllById(
                savedPerformanceSeats.stream().map(PerformanceSeatEntity::getId).toList()
        );
        // then
        assertThat(reservedSeats).allMatch(seat -> seat.getState() == PerformanceSeatState.RESERVED);
        final List<ReservationEntity> reservations = reservationRepository.findAll();
        assertThat(reservations).hasSize(1);
        assertThat(reservations.getFirst().getMemberId()).isEqualTo(savedMember.getId());

        final List<ReservationDetailEntity> details = reservationDetailRepository.findAll();
        assertThat(details).hasSize(2);
    }
}
