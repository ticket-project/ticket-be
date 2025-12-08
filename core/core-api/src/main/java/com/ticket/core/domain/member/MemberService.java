package com.ticket.core.domain.member;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberFinder memberFinder;

    public MemberService(final MemberFinder memberFinder) {
        this.memberFinder = memberFinder;
    }

    public Member findById(final Long memberId) {
        return memberFinder.find(memberId);
    }

}
