package com.ticket.core.domain.auth.oauth2;

import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.domain.member.MemberRepository;
import com.ticket.core.domain.member.MemberSocialAccount;
import com.ticket.core.domain.member.MemberSocialAccountRepository;
import com.ticket.core.domain.member.vo.Email;
import com.ticket.core.enums.Role;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final MemberRepository memberRepository;
    private final MemberSocialAccountRepository memberSocialAccountRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(final OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        final OAuth2User oauth2User = delegate.loadUser(userRequest);
        final String registrationId = userRequest.getClientRegistration().getRegistrationId();
        final OAuth2UserInfo userInfo = OAuth2UserInfoFactory.create(registrationId, oauth2User.getAttributes());

        final Member member = getOrCreateMember(userInfo);
        return new MemberPrincipal(member.getId(), member.getRole(), oauth2User.getAttributes());
    }

    private Member getOrCreateMember(final OAuth2UserInfo userInfo) {
        return memberSocialAccountRepository.findBySocialProviderAndSocialId(userInfo.provider(), userInfo.providerId())
                .map(MemberSocialAccount::getMember)
                .orElseGet(() -> createOrLinkMember(userInfo));
    }

    private Member createOrLinkMember(final OAuth2UserInfo userInfo) {
        final String email = resolveEmail(userInfo);

        return memberRepository.findByEmail_Email(email)
                .map(existingMember -> linkSocialAccount(existingMember, userInfo))
                .orElseGet(() -> createSocialMember(userInfo, email));
    }

    private Member linkSocialAccount(final Member existingMember, final OAuth2UserInfo userInfo) {
        return memberSocialAccountRepository.findByMemberAndSocialProvider(existingMember, userInfo.provider())
                .map(linkedAccount -> validateSameSocialAccount(linkedAccount, userInfo))
                .orElseGet(() -> addSocialAccount(existingMember, userInfo));
    }

    private Member validateSameSocialAccount(final MemberSocialAccount linkedAccount, final OAuth2UserInfo userInfo) {
        if (!linkedAccount.isSameSocialId(userInfo.providerId())) {
            throw new CoreException(ErrorType.MEMBER_DUPLICATE_EMAIL, "Email is already linked to another social account.");
        }
        return linkedAccount.getMember();
    }

    private Member addSocialAccount(final Member existingMember, final OAuth2UserInfo userInfo) {
        memberSocialAccountRepository.save(MemberSocialAccount.create(
                existingMember,
                userInfo.provider(),
                userInfo.providerId()
        ));
        return existingMember;
    }

    private Member createSocialMember(final OAuth2UserInfo userInfo, final String email) {
        final String displayName = resolveName(userInfo);
        final Member member = Member.createSocialMember(
                Email.create(email),
                displayName,
                Role.MEMBER
        );
        final Member savedMember = memberRepository.save(member);
        memberSocialAccountRepository.save(MemberSocialAccount.create(
                savedMember,
                userInfo.provider(),
                userInfo.providerId()
        ));
        return savedMember;
    }

    private String resolveEmail(final OAuth2UserInfo userInfo) {
        if (StringUtils.hasText(userInfo.email())) {
            return userInfo.email().trim().toLowerCase();
        }
        return userInfo.provider().name().toLowerCase() + "_" + userInfo.providerId() + "@social.ticket";
    }

    private String resolveName(final OAuth2UserInfo userInfo) {
        if (StringUtils.hasText(userInfo.name())) {
            return userInfo.name().trim();
        }
        return userInfo.provider().name().toLowerCase() + "_" + userInfo.providerId();
    }
}
