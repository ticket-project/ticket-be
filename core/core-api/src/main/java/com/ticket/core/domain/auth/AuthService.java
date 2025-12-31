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
        if (memberRepository.existsByEmail(addMember.getEmailValue())) {
            throw new CoreException(ErrorType.MEMBER_DUPLICATE_EMAIL);
        }
        final Member member = new Member(
                addMember.getEmailValue(),
                passwordService.encode(addMember.getPassword()),
                addMember.getName(),
                addMember.getRole()
        );

        try {
            return memberRepository.save(member).getId();
        } catch (DataIntegrityViolationException e) {
            throw new CoreException(ErrorType.MEMBER_DUPLICATE_EMAIL);
        }
    }

    public Member login(final Email email, final Password password) {
        final Member foundMember = memberRepository.findByEmail(email.getValue())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        if (!passwordService.matches(password.getValue(), foundMember.getPassword())) {
            throw new CoreException(ErrorType.MEMBER_NOT_MATCH_PASSWORD);
        }
        return new Member(foundMember.getId(), foundMember.getEmail(), foundMember.getName(), foundMember.getRole());
    }
}
