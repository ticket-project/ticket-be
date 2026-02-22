package com.ticket.core.domain.auth.oauth2;

import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberRepository;
import com.ticket.core.domain.member.MemberSocialAccount;
import com.ticket.core.domain.member.MemberSocialAccountRepository;
import com.ticket.core.domain.member.vo.Email;
import com.ticket.core.enums.Role;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import static com.ticket.core.enums.EntityStatus.ACTIVE;

@Service
@RequiredArgsConstructor
@Transactional
public class OAuth2MemberProvisioningService {

    private final MemberRepository memberRepository;
    private final MemberSocialAccountRepository memberSocialAccountRepository;

    public Member getOrCreateMember(final OAuth2UserInfo userInfo) {
        return memberSocialAccountRepository.findBySocialProviderAndSocialIdAndStatusAndMember_Status(
                        userInfo.provider(),
                        userInfo.providerId(),
                        ACTIVE,
                        ACTIVE
                )
                .map(MemberSocialAccount::getMember)
                .orElseGet(() -> createOrLinkMember(userInfo));
    }

    private Member createOrLinkMember(final OAuth2UserInfo userInfo) {
        final String email = resolveEmail(userInfo);

        return memberRepository.findByEmail_EmailAndStatus(email, ACTIVE)
                .map(existingMember -> linkSocialAccount(existingMember, userInfo))
                .orElseGet(() -> createSocialMember(userInfo, email));
    }

    private Member linkSocialAccount(final Member existingMember, final OAuth2UserInfo userInfo) {
        return memberSocialAccountRepository.findByMemberAndSocialProviderAndStatus(existingMember, userInfo.provider(), ACTIVE)
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
