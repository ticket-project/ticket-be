package com.ticket.domain.member;

import com.ticket.domain.member.vo.Email;
import com.ticket.storage.db.core.MemberEntity;
import com.ticket.storage.db.core.MemberRepository;
import com.ticket.support.exception.DuplicateEmailException;
import com.ticket.support.exception.ErrorType;
import com.ticket.support.exception.NotFoundException;
import com.ticket.support.exception.PasswordInvalidException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyValidator passwordPolicyValidator;

    public MemberService(final MemberRepository memberRepository, final PasswordEncoder passwordEncoder, final PasswordPolicyValidator passwordPolicyValidator) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicyValidator = passwordPolicyValidator;
    }

    @Transactional
    public Long register(final AddMember addMember) {
        //비밀번호 정책 관련 많이 나올듯 해서 분리함.
        passwordPolicyValidator.validateAdd(addMember.getPassword());
        //이메일 정책은 중복 밖에 없을듯 해서 굳이 분리하지 않음.
        if (memberRepository.existsByEmailAddress(addMember.getEmail())) {
            throw new DuplicateEmailException(ErrorType.DUPLICATE_EMAIL_ERROR);
        }
        final MemberEntity savedMember = memberRepository.save(new MemberEntity(
                addMember.getEmail(),
                passwordEncoder.encode(addMember.getPassword()),
                addMember.getName()
        ));
        return savedMember.getId();
    }

    public Member findById(final Long memberId) {
        final MemberEntity memberEntity = memberRepository.findById(memberId).orElseThrow(() -> new NotFoundException(ErrorType.NOT_FOUND));
        return new Member(memberEntity.getId(), new Email(memberEntity.getEmail()), memberEntity.getName());
    }

    public Member login(final String email, final String password) {
        final MemberEntity foundMemberEntity = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorType.NOT_FOUND));

        if (!passwordEncoder.matches(password, foundMemberEntity.getPassword())) {
            throw new PasswordInvalidException(ErrorType.INVALID_PASSWORD);
        }
        return new Member(foundMemberEntity.getId(), new Email(foundMemberEntity.getEmail()), foundMemberEntity.getName());
    }
}
