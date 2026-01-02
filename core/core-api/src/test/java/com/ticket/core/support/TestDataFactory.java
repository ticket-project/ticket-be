package com.ticket.core.support;

import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.vo.Email;
import com.ticket.core.domain.member.vo.EncodedPassword;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performanceseat.PerformanceSeat;
import com.ticket.core.domain.reservation.Reservation;
import com.ticket.core.domain.reservation.ReservationDetail;
import com.ticket.core.domain.seat.Seat;
import com.ticket.core.enums.PerformanceSeatState;
import com.ticket.core.enums.PerformanceState;
import com.ticket.core.enums.Role;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TestDataFactory {

    public static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 1, 1, 0, 0);

    // ===== Member =====
    public static Member createMember() {
        return createMember("test@example.com", "password123", "테스트유저", Role.MEMBER);
    }

    public static Member createMember(String email, String password, String name, Role role) {
        return new Member(Email.create(email), EncodedPassword.create(password), name, role);
    }

    public static Member createAdminMember() {
        return createMember("admin@example.com", "password123", "관리자", Role.ADMIN);
    }

    // ===== Performance (회차) =====
    public static Performance createPerformance() {
        return createPerformance(1L, PerformanceState.OPEN);
    }

    public static Performance createPerformance(Long showId, PerformanceState state) {
        return new Performance(
                showId,
                1L,
                BASE_TIME.plusDays(7),
                BASE_TIME.plusDays(7).plusHours(2),
                BASE_TIME.minusDays(1),
                BASE_TIME.plusDays(6),
                4,
                300,
                state
        );
    }

    public static Performance createClosedPerformance(Long showId) {
        return createPerformance(showId, PerformanceState.CLOSE);
    }

    // ===== Seat (좌석) =====
    public static Seat createSeat(Long id, String row, String col) {
        return new Seat(id, row, col);
    }

    public static List<Seat> createSeats(int count) {
        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            seats.add(createSeat((long) i, "A", String.valueOf(i)));
        }
        return seats;
    }

    // ===== PerformanceSeat (회차_좌석) =====
    public static PerformanceSeat createPerformanceSeat(Long performanceId, Long seatId, PerformanceSeatState state, LocalDateTime holdExpireAt, Long holdByMemberId, String holdToken) {
        return new PerformanceSeat(performanceId, seatId, state, holdExpireAt, holdByMemberId, holdToken);
    }

    public static PerformanceSeat createAvailableSeat(Long performanceId, Long seatId) {
        return createPerformanceSeat(performanceId, seatId, PerformanceSeatState.AVAILABLE, null , null, null);
    }

    public static List<PerformanceSeat> createAvailableSeats(Long performanceId, List<Long> seatIds) {
        return seatIds.stream()
                .map(seatId -> createAvailableSeat(performanceId, seatId))
                .toList();
    }

    // ===== Reservation (예매) =====
    public static Reservation createReservation(Long memberId, Long performanceId) {
        return new Reservation(memberId, performanceId);
    }

    // ===== ReservationDetail (예매 상세) =====
    public static ReservationDetail createReservationDetail(Long reservationId, Long performanceSeatId) {
        return new ReservationDetail(reservationId, performanceSeatId);
    }

}
