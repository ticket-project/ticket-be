package com.ticket.core.domain.auth;

import com.ticket.core.domain.member.AddMember;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberRepository;
import com.ticket.core.domain.member.PasswordService;
import com.ticket.core.domain.member.vo.Email;
import com.ticket.core.domain.member.vo.Password;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final MemberRepository memberRepository;
    private final PasswordService passwordService;

    public AuthService(final MemberRepository memberRepository, final PasswordService passwordService) {
        this.memberRepository = memberRepository;
        this.passwordService = passwordService;
    }

    @Transactional
    public Long register(final AddMember addMember) {
        final Member member = new Member(
                Email.create(addMember.getEmail()),
                Password.create(passwordService.encode(addMember.getPassword())),
                addMember.getName(),
                addMember.getRole()
        );

        try {
            return memberRepository.save(member).getId();
        } catch (DataIntegrityViolationException e) {
            // 로깅 추가로 실제 중복 시도 빈도 모니터링
            log.warn("이메일 중복 회원가입 시도: {}", addMember.getEmail());
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
