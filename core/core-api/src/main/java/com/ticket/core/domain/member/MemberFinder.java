package com.ticket.core.domain.member;

import com.ticket.core.support.exception.ErrorType;
import com.ticket.core.support.exception.NotFoundException;
import org.springframework.stereotype.Component;

@Component
public class MemberFinder {

    private final MemberRepository memberRepository;

    public MemberFinder(final MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member find(final Long id) {
        final Member member = memberRepository.findById(id).orElseThrow(() -> new NotFoundException(ErrorType.NOT_FOUND_DATA));
        return new Member(member.getId(), member.getEmail(), member.getName(), member.getRole());
    }
}
