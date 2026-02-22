package com.ticket.core.domain.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberFinder memberFinder;
    private final MemberSocialAccountRepository memberSocialAccountRepository;

    public Member findById(final Long memberId) {
        return memberFinder.find(memberId);
    }

    @Transactional
    public void withdraw(final Long memberId) {
        final Member member = memberFinder.find(memberId);
        memberSocialAccountRepository.findAllByMember(member).forEach(MemberSocialAccount::withdraw);
        member.withdraw();
    }

}
