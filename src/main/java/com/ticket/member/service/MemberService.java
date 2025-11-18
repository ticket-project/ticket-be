package com.ticket.member.service;

import com.ticket.exception.DuplicateEmailException;
import com.ticket.member.Member;
import com.ticket.member.dto.MemberCreateRequest;
import com.ticket.member.dto.MemberResponse;
import com.ticket.member.repository.MemberRepository;
import com.ticket.member.vo.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MemberResponse register(final MemberCreateRequest.MemberCreateCommand memberCreateCommand) {
        if (memberRepository.existsByEmailAddress(memberCreateCommand.getEmail())) {
            throw new DuplicateEmailException("중복 이메일은 허용되지 않습니다.");
        }
        final Member member = Member.register(new Email(memberCreateCommand.getEmail()), passwordEncoder.encode(memberCreateCommand.getPassword()), memberCreateCommand.getName());
        memberRepository.save(member);
        return MemberResponse.of(member);
    }
}
