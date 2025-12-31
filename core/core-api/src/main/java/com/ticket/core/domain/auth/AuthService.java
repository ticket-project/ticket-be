package com.ticket.core.domain.auth;

import com.ticket.core.domain.member.AddMember;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberRepository;
import com.ticket.core.domain.member.PasswordService;
import com.ticket.core.domain.member.vo.Email;
import com.ticket.core.domain.member.vo.Password;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordService passwordService;

    public AuthService(final MemberRepository memberRepository, final PasswordService passwordService) {
        this.memberRepository = memberRepository;
        this.passwordService = passwordService;
    }

    @Transactional
    public Long register(final AddMember addMember) {
        if (memberRepository.existsByEmail(addMember.getEmail())) {
            throw new CoreException(ErrorType.MEMBER_DUPLICATE_EMAIL);
        }
        final Member member = new Member(
                Email.create(addMember.getEmail()),
                Password.create(passwordService.encode(addMember.getPassword())),
                addMember.getName(),
                addMember.getRole()
        );

        try {
            return memberRepository.save(member).getId();
        } catch (DataIntegrityViolationException e) {
            throw new CoreException(ErrorType.MEMBER_DUPLICATE_EMAIL);
        }
    }

    public Member login(final String email, final String password) {
        final Member foundMember = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        if (!passwordService.matches(password, foundMember.getPassword().getPassword())) {
            throw new CoreException(ErrorType.MEMBER_NOT_MATCH_PASSWORD);
        }
        return foundMember;
    }
}
