package com.ticket.core.domain.member.command;

import com.ticket.core.domain.member.model.Member;
import com.ticket.core.domain.member.model.MemberSocialAccount;
import com.ticket.core.domain.member.query.MemberFinder;
import com.ticket.core.domain.member.repository.MemberSocialAccountRepository;
import com.ticket.core.domain.member.model.SocialProvider;
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
