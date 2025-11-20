package com.ticket.domain.member;

import com.ticket.exception.DuplicateEmailException;
import com.ticket.storage.db.core.MemberEntity;
import com.ticket.storage.db.core.MemberRepository;
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
            throw new DuplicateEmailException("중복 이메일은 허용되지 않습니다.");
        }
        final MemberEntity savedMember = memberRepository.save(new MemberEntity(
                addMember.getEmail(),
                passwordEncoder.encode(addMember.getPassword()),
                addMember.getName()
        ));
        return savedMember.getId();
    }
}
