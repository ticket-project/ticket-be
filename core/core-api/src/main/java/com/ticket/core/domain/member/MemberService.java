package com.ticket.core.domain.member;

import com.ticket.core.domain.member.vo.Email;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import com.ticket.storage.db.core.MemberEntity;
import com.ticket.storage.db.core.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberFinder memberFinder;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyValidator passwordPolicyValidator;

    public MemberService(final MemberRepository memberRepository, final MemberFinder memberFinder, final PasswordEncoder passwordEncoder, final PasswordPolicyValidator passwordPolicyValidator) {
        this.memberRepository = memberRepository;
        this.memberFinder = memberFinder;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicyValidator = passwordPolicyValidator;
    }

    @Transactional
    public Long register(final AddMember addMember) {
        //비밀번호 정책 관련 많이 나올듯 해서 분리함.
        passwordPolicyValidator.validateAdd(addMember.getPassword());
        //이메일 정책은 중복 밖에 없을듯 해서 굳이 분리하지 않음.
        if (memberRepository.existsByEmail(addMember.getEmail())) {
            throw new CoreException(ErrorType.DUPLICATE_EMAIL_ERROR);
        }
        final MemberEntity savedMember = memberRepository.save(new MemberEntity(
                addMember.getEmail(),
                passwordEncoder.encode(addMember.getPassword()),
                addMember.getName(),
                addMember.getRole()
        ));
        return savedMember.getId();
    }

    public Member findById(final Long memberId) {
        return memberFinder.find(memberId);
    }

    public Member login(final String email, final String password) {
        final MemberEntity foundMemberEntity = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND));

        if (!passwordEncoder.matches(password, foundMemberEntity.getPassword())) {
            throw new CoreException(ErrorType.INVALID_PASSWORD);
        }
        return new Member(foundMemberEntity.getId(), new Email(foundMemberEntity.getEmail()), foundMemberEntity.getName(), foundMemberEntity.getRole());
    }
}
