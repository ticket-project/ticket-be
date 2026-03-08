package com.ticket.core.domain.member;

import com.ticket.core.domain.auth.oauth2.KakaoUnlinkService;
import com.ticket.core.enums.SocialProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberFinder memberFinder;
    private final MemberSocialAccountRepository memberSocialAccountRepository;
    private final KakaoUnlinkService kakaoUnlinkService;

    public Member findById(final Long memberId) {
        return memberFinder.findActiveMemberById(memberId);
    }

    /**
     * 회원 탈퇴
     * 1단계: DB 탈퇴 처리 (트랜잭션) — 실패 시 전체 롤백
     * 2단계: 카카오 unlink (트랜잭션 밖) — 실패해도 탈퇴는 완료
     */
    public void withdraw(final Long memberId) {
        final List<String> kakaoSocialIds = withdrawInTransaction(memberId);
        unlinkKakaoAccountsSafely(kakaoSocialIds);
    }

    @Transactional
    protected List<String> withdrawInTransaction(final Long memberId) {
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

    /**
     * 카카오 unlink — best-effort 처리
     * 실패해도 DB 탈퇴는 이미 커밋된 상태이므로 서비스 영향 없음
     */
    private void unlinkKakaoAccountsSafely(final List<String> kakaoSocialIds) {
        kakaoSocialIds.forEach(socialId -> {
            try {
                kakaoUnlinkService.unlinkByUserId(socialId);
            } catch (Exception e) {
                log.warn("카카오 unlink 실패 (socialId={}), 추후 수동처리 필요", socialId, e);
            }
        });
    }
}