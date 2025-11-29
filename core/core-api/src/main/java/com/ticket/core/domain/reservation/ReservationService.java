package com.ticket.core.domain.reservation;

import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberService;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceService;
import com.ticket.core.domain.seat.Seat;
import com.ticket.core.domain.seat.SeatService;
import com.ticket.storage.db.core.ReservationEntity;
import com.ticket.storage.db.core.ReservationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final MemberService memberService;
    private final PerformanceService performanceService;
    private final SeatService seatService;

    public ReservationService(final ReservationRepository reservationRepository,
                              final MemberService memberService,
                              final PerformanceService performanceService,
                              final SeatService seatService) {
        this.reservationRepository = reservationRepository;
        this.memberService = memberService;
        this.performanceService = performanceService;
        this.seatService = seatService;
    }

    public Long reserve(final AddReservation addReservation) {
        final Member foundMember = memberService.findById(addReservation.getMemberId());
        final Performance foundPerformance = performanceService.findById(addReservation.getPerformanceId());
        final List<Long> seatIds = addReservation.getSeatIds();
        final List<Long> foundSeatIds = seatIds.stream()
                .map(seatService::findById)
                .map(Seat::getId)
                .toList();

        final ReservationEntity savedReservation = reservationRepository.save(new ReservationEntity(foundMember.getId(), foundPerformance.getId(), foundSeatIds));
        return savedReservation.getId();
    }
}
