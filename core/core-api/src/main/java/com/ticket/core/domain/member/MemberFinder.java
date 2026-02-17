package com.ticket.core.domain.member;

import com.ticket.core.support.exception.ErrorType;
import com.ticket.core.support.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberFinder {

    private final MemberRepository memberRepository;

    public Member find(final Long id) {
        return memberRepository.findById(id).orElseThrow(() -> new NotFoundException(ErrorType.NOT_FOUND_DATA));
    }
}
