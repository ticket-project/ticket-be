package com.ticket.core.domain.auth;

import com.ticket.core.domain.member.AddMember;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.PasswordService;
import com.ticket.core.domain.member.vo.Email;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import com.ticket.storage.db.core.MemberEntity;
import com.ticket.storage.db.core.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
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
            throw new CoreException(ErrorType.DUPLICATE_EMAIL_ERROR);
        }
        final MemberEntity savedMember = memberRepository.save(new MemberEntity(
                addMember.getEmailValue(),
                passwordService.encode(addMember.getPassword()),
                addMember.getName(),
                addMember.getRole()
        ));
        return savedMember.getId();
    }

    public Member login(final String email, final String password) {
        final MemberEntity foundMemberEntity = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND));

        if (!passwordService.matches(password, foundMemberEntity.getPassword())) {
            throw new CoreException(ErrorType.NOT_MATCH_PASSWORD);
        }
        return new Member(foundMemberEntity.getId(), new Email(foundMemberEntity.getEmail()), foundMemberEntity.getName(), foundMemberEntity.getRole());
    }
}
