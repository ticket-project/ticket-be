package com.ticket.core.domain.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationDetailRepository extends JpaRepository<ReservationDetail, Long> {
    @Query("SELECT COUNT(rd) FROM ReservationDetail rd " +
            "JOIN Reservation r ON rd.reservationId = r.id " +
            "WHERE r.memberId = :memberId AND r.performanceId = :performanceId")
    long countByMemberIdAndPerformanceId(@Param("memberId") Long memberId,
                                         @Param("performanceId") Long performanceId);}
