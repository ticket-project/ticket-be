package com.ticket.core.domain.auth;

import com.ticket.core.domain.member.AddMember;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberRepository;
import com.ticket.core.domain.member.vo.EncodedPassword;
import com.ticket.core.domain.member.vo.RawPassword;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final MemberRepository memberRepository;
    private final PasswordService passwordService;

    @Transactional
    public Long register(final AddMember addMember) {
        final Member member = new Member(
                addMember.getEmail(),
                EncodedPassword.create(passwordService.encode(addMember.getRawPassword().getPassword())),
                addMember.getName(),
                addMember.getRole()
        );

        try {
            return memberRepository.save(member).getId();
        } catch (DataIntegrityViolationException e) {
            log.warn("이메일 중복 회원가입 시도: {}", addMember.getEmail());
            throw new CoreException(ErrorType.MEMBER_DUPLICATE_EMAIL);
        }
    }

    public Member login(final String email, final String password) {
        final Member foundMember = memberRepository.findByEmail_Email(email)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        if (!passwordService.matches(RawPassword.create(password), foundMember.getEncodedPassword())) {
            throw new CoreException(ErrorType.MEMBER_NOT_MATCH_PASSWORD);
        }
        return foundMember;
    }
}
