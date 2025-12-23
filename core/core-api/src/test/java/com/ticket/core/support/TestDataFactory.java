package com.ticket.core.support;

import com.ticket.core.enums.PerformanceSeatState;
import com.ticket.core.enums.PerformanceState;
import com.ticket.core.enums.Role;
import com.ticket.storage.db.core.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TestDataFactory {

    // ===== Member =====
    public static MemberEntity createMember() {
        return createMember("test@example.com", "password123", "테스트유저", Role.MEMBER);
    }

    public static MemberEntity createMember(String email, String password, String name, Role role) {
        return new MemberEntity(email, password, name, role);
    }

    public static MemberEntity createAdminMember() {
        return createMember("admin@example.com", "password123", "관리자", Role.ADMIN);
    }

    // ===== Performance (회차) =====
    public static PerformanceEntity createPerformance() {
        return createPerformance(1L, PerformanceState.OPEN);
    }

    public static PerformanceEntity createPerformance(Long showId, PerformanceState state) {
        LocalDateTime now = LocalDateTime.now();
        return new PerformanceEntity(
                showId,
                now.plusDays(7),
                now.plusDays(7).plusHours(2),
                now.minusDays(1),
                now.plusDays(6),
                4,
                state
        );
    }

    public static PerformanceEntity createClosedPerformance(Long showId) {
        return createPerformance(showId, PerformanceState.CLOSE);
    }

    // ===== Seat (좌석) =====
    public static SeatEntity createSeat(Long id, String row, String col) {
        return new SeatEntity(id, row, col);
    }

    public static List<SeatEntity> createSeats(int count) {
        List<SeatEntity> seats = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            seats.add(createSeat((long) i, "A", String.valueOf(i)));
        }
        return seats;
    }

    // ===== PerformanceSeat (회차_좌석) =====
    public static PerformanceSeatEntity createPerformanceSeat(Long performanceId, Long seatId, PerformanceSeatState state) {
        return new PerformanceSeatEntity(performanceId, seatId, state);
    }

    public static PerformanceSeatEntity createAvailableSeat(Long performanceId, Long seatId) {
        return createPerformanceSeat(performanceId, seatId, PerformanceSeatState.AVAILABLE);
    }

    public static List<PerformanceSeatEntity> createAvailableSeats(Long performanceId, List<Long> seatIds) {
        List<PerformanceSeatEntity> seats = new ArrayList<>();
        for (Long seatId : seatIds) {
            seats.add(createAvailableSeat(performanceId, seatId));
        }
        return seats;
    }

    // ===== Reservation (예매) =====
    public static ReservationEntity createReservation(Long memberId, Long performanceId) {
        return new ReservationEntity(memberId, performanceId);
    }

    // ===== ReservationDetail (예매 상세) =====
    public static ReservationDetailEntity createReservationDetail(Long reservationId, Long performanceSeatId) {
        return new ReservationDetailEntity(reservationId, performanceSeatId);
    }

}
