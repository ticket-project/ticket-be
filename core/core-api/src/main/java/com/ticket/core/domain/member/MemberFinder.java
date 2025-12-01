package com.ticket.core.domain.member;

import com.ticket.core.domain.member.vo.Email;
import com.ticket.core.support.exception.ErrorType;
import com.ticket.core.support.exception.NotFoundException;
import com.ticket.storage.db.core.MemberEntity;
import com.ticket.storage.db.core.MemberRepository;
import org.springframework.stereotype.Component;

@Component
public class MemberFinder {

    private final MemberRepository memberRepository;

    public MemberFinder(final MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member find(final Long id) {
        final MemberEntity memberEntity = memberRepository.findById(id).orElseThrow(() -> new NotFoundException(ErrorType.NOT_FOUND));
        return new Member(memberEntity.getId(), new Email(memberEntity.getEmail()), memberEntity.getName());
    }
}
