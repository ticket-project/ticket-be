package com.ticket.member.service;

import com.ticket.exception.DuplicateEmailException;
import com.ticket.member.Member;
import com.ticket.member.PasswordPolicy;
import com.ticket.member.dto.MemberCreateRequest;
import com.ticket.member.dto.MemberResponse;
import com.ticket.member.repository.MemberRepository;
import com.ticket.member.vo.Email;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(final MemberRepository memberRepository, final PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public MemberResponse register(final MemberCreateRequest.MemberCreateCommand command) {
        PasswordPolicy.validate(command.getPassword());
        final Email email = new Email(command.getEmail());
        if (memberRepository.existsByEmailAddress(email.getEmail())) {
            throw new DuplicateEmailException("중복 이메일은 허용되지 않습니다.");
        }
        final Member member = Member.register(email, passwordEncoder.encode(command.getPassword()), command.getName());
        return MemberResponse.of(memberRepository.save(member));
    }
}
