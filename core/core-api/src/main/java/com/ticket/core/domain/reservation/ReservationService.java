package com.ticket.core.domain.reservation;

import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberFinder;
import com.ticket.core.enums.EntityStatus;
import com.ticket.core.enums.PerformanceSeatState;
import com.ticket.core.enums.PerformanceState;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import com.ticket.core.support.exception.NotFoundException;
import com.ticket.storage.db.core.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReservationService {

    private final MemberFinder memberFinder;
    private final ShowRepository showRepository;
    private final PerformanceRepository performanceRepository;
    private final PerformanceSeatRepository performanceSeatRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationDetailRepository reservationDetailRepository;

    public ReservationService(final MemberFinder memberFinder,
                              final ShowRepository showRepository,
                              final PerformanceRepository performanceRepository,
                              final PerformanceSeatRepository performanceSeatRepository,
                              final ReservationRepository reservationRepository,
                              final ReservationDetailRepository reservationDetailRepository
    ) {
        this.memberFinder = memberFinder;
        this.showRepository = showRepository;
        this.performanceRepository = performanceRepository;
        this.performanceSeatRepository = performanceSeatRepository;
        this.reservationRepository = reservationRepository;
        this.reservationDetailRepository = reservationDetailRepository;
    }

    @Transactional
    public void reserve(final NewReservation newReservation) {
        final Member foundMember = memberFinder.find(newReservation.getMemberId());
        final ShowEntity foundShow = showRepository.findByIdAndStatus(newReservation.getShowId(), EntityStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException(ErrorType.NOT_FOUND_DATA));
        final PerformanceEntity foundPerformance = findPerformance(newReservation.getPerformanceId(), foundShow.getId());

        //예매 요청한 좌석 수가 인당 최대 좌석 예매 수보다 많으면 예외 <-- 주석을 달하야 하는 것 부터가 큰 문제다.
        if (newReservation.getSeatIds().size() > foundPerformance.getMaxCanReserveCount()) {
            throw new CoreException(ErrorType.OVER_RESERVATION_COUNT);
        }
        final List<Long> reservationIds = reservationRepository.findAllByMemberIdAndPerformanceId(foundMember.getId(), foundPerformance.getId()).stream()
                .map(ReservationEntity::getId)
                .toList();
        final long reservedCount = reservationDetailRepository.findAllByReservationIdIn(reservationIds).size();
        if (foundPerformance.isOverCount(reservedCount + newReservation.getSeatIds().size())) { //이거 이렇게? 아니면 도메인 내부에서 예외 터트리는 것 까지? but, 지금은 entity라 불가능하다.
            throw new CoreException(ErrorType.OVER_RESERVATION_COUNT);
        };

        final List<PerformanceSeatEntity> foundPerformanceSeats = findPerformanceSeats(newReservation, foundPerformance.getId());
        //예매 가능한 좌석 수와 요청한 좌석 수가 다르면 예외 <-- 주석을 달하야 하는 것 부터가 큰 문제다.
        if (foundPerformanceSeats.size() != newReservation.getSeatIds().size()) {
            throw new CoreException(ErrorType.SEAT_COUNT_MISMATCH);
        }

        foundPerformanceSeats.forEach(PerformanceSeatEntity::reserve);

        final ReservationEntity savedReservation = saveReservation(foundMember, foundPerformance);
        saveReservationDetails(foundPerformanceSeats, savedReservation.getId());
    }

    private PerformanceEntity findPerformance(final Long performanceId, final Long showId) {
        return performanceRepository.findByIdAndShowIdAndStateAndStatus(
                        performanceId,
                        showId,
                        PerformanceState.OPEN,
                        EntityStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException(ErrorType.NOT_FOUND_DATA));
    }

    private List<PerformanceSeatEntity> findPerformanceSeats(final NewReservation newReservation, final Long performanceId) {
        return performanceSeatRepository.findByPerformanceIdAndSeatIdInAndState(
                performanceId,
                newReservation.getSeatIds(),
                PerformanceSeatState.AVAILABLE
        );
    }

    private ReservationEntity saveReservation(final Member foundMember, final PerformanceEntity foundPerformance) {
        return reservationRepository.save(new ReservationEntity(
                foundMember.getId(),
                foundPerformance.getId()
        ));
    }

    private void saveReservationDetails(final List<PerformanceSeatEntity> foundPerformanceSeats, final Long reservationId) {
        reservationDetailRepository.saveAll(
                foundPerformanceSeats.stream()
                        .map(p -> new ReservationDetailEntity(reservationId, p.getId()))
                        .toList()
        );
    }
}
