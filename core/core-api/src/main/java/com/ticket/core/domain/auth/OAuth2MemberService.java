package com.ticket.core.domain.auth;

import com.ticket.core.domain.auth.oauth.OAuth2UserInfo;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberRepository;
import com.ticket.core.domain.member.vo.Email;
import com.ticket.core.enums.Role;
import com.ticket.core.enums.SocialProvider;
import com.ticket.core.support.exception.AuthException;
import com.ticket.core.support.exception.ErrorType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class OAuth2MemberService {

    private final MemberRepository memberRepository;

    public OAuth2MemberService(final MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member upsertMember(final OAuth2UserInfo userInfo) {
        validate(userInfo);

        final SocialProvider provider = userInfo.getProvider();
        final String providerId = userInfo.getProviderId();
        final String email = userInfo.getEmail();

        final Optional<Member> byProvider = memberRepository.findByProviderAndProviderId(provider, providerId);
        if (byProvider.isPresent()) {
            final Member member = byProvider.get();
            member.updateSocialInfo(provider, providerId, userInfo.getImageUrl(), userInfo.getName());
            return member;
        }

        final Optional<Member> byEmail = memberRepository.findByEmail_Email(email);
        if (byEmail.isPresent()) {
            final Member member = byEmail.get();
            member.updateSocialInfo(provider, providerId, userInfo.getImageUrl(), userInfo.getName());
            return member;
        }

        final Member member = new Member(
                Email.create(email),
                null,
                userInfo.getName(),
                Role.MEMBER,
                provider,
                providerId,
                userInfo.getImageUrl()
        );
        return memberRepository.save(member);
    }

    private void validate(final OAuth2UserInfo userInfo) {
        if (userInfo.getProviderId() == null || userInfo.getProviderId().isBlank()) {
            throw new AuthException(ErrorType.INVALID_REQUEST, "Missing providerId");
        }
        if (userInfo.getEmail() == null || userInfo.getEmail().isBlank()) {
            throw new AuthException(ErrorType.INVALID_REQUEST, "Missing email");
        }
    }
}
