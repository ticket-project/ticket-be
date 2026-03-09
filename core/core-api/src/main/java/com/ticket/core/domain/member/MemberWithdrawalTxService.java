package com.ticket.core.domain.member;

import com.ticket.core.enums.SocialProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberWithdrawalTxService {

    private final MemberFinder memberFinder;
    private final MemberSocialAccountRepository memberSocialAccountRepository;

    @Transactional
    public List<String> withdraw(final Long memberId) {
        final Member member = memberFinder.findActiveMemberById(memberId);
        final List<MemberSocialAccount> socialAccounts = memberSocialAccountRepository.findAllByMemberAndDeletedAtIsNull(member);

        final List<String> kakaoSocialIds = socialAccounts.stream()
                .filter(account -> account.getSocialProvider() == SocialProvider.KAKAO)
                .map(MemberSocialAccount::getSocialId)
                .toList();

        socialAccounts.forEach(MemberSocialAccount::withdraw);
        member.withdraw();

        return kakaoSocialIds;
    }
}