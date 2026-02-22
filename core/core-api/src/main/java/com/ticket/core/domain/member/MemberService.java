package com.ticket.core.domain.member;

import com.ticket.core.domain.auth.oauth2.KakaoUnlinkService;
import com.ticket.core.enums.SocialProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ticket.core.enums.EntityStatus.ACTIVE;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberFinder memberFinder;
    private final MemberSocialAccountRepository memberSocialAccountRepository;
    private final KakaoUnlinkService kakaoUnlinkService;

    public Member findById(final Long memberId) {
        return memberFinder.find(memberId);
    }

    @Transactional
    public void withdraw(final Long memberId) {
        final Member member = memberFinder.find(memberId);
        final List<MemberSocialAccount> socialAccounts = memberSocialAccountRepository.findAllByMember(member);
        unlinkKakaoAccounts(socialAccounts);
        socialAccounts.forEach(MemberSocialAccount::withdraw);
        member.withdraw();
    }

    private void unlinkKakaoAccounts(final List<MemberSocialAccount> socialAccounts) {
        socialAccounts.stream()
                .filter(account -> account.getStatus() == ACTIVE)
                .filter(account -> account.getSocialProvider() == SocialProvider.KAKAO)
                .forEach(account -> kakaoUnlinkService.unlinkByUserId(account.getSocialId()));
    }

}
