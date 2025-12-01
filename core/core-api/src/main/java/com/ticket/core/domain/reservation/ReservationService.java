package com.ticket.core.domain.reservation;

import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberFinder;
import org.springframework.stereotype.Service;

@Service
public class ReservationService {

    private final MemberFinder memberFinder;

    public ReservationService(final MemberFinder memberFinder) {
        this.memberFinder = memberFinder;
    }

    public void reserve(final Member member, final NewReservation newReservation) {
        final Member foundMember = memberFinder.find(member.getId());

    }
}
