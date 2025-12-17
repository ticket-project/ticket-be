package com.ticket.core.domain.reservation;

import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberFinder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationService {

    private final MemberFinder memberFinder;
    private final ReservationValidator reservationValidator;
    private final ReservationManager reservationManager;

    public ReservationService(final MemberFinder memberFinder,
                              final ReservationValidator reservationValidator,
                              final ReservationManager reservationManager
) {
        this.memberFinder = memberFinder;
        this.reservationValidator = reservationValidator;
        this.reservationManager = reservationManager;
    }

    @Transactional
    public void addReservation(final Long memberId, final NewReservation newReservation) {
        final Member foundMember = memberFinder.find(memberId);

        final ReservationKey reservationKey = reservationValidator.validateNew(foundMember, newReservation);
        reservationManager.add(foundMember, reservationKey);
    }




}
