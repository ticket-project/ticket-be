package com.ticket.core.domain.member;

import com.ticket.core.domain.auth.oauth2.KakaoUnlinkService;
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
    private final MemberWithdrawalTxService memberWithdrawalTxService;
    private final KakaoUnlinkService kakaoUnlinkService;

    public Member findById(final Long memberId) {
        return memberFinder.findActiveMemberById(memberId);
    }

    public void withdraw(final Long memberId) {
        final List<String> kakaoSocialIds = memberWithdrawalTxService.withdraw(memberId);
        unlinkKakaoAccountsSafely(kakaoSocialIds);
    }

    private void unlinkKakaoAccountsSafely(final List<String> kakaoSocialIds) {
        kakaoSocialIds.forEach(socialId -> {
            try {
                kakaoUnlinkService.unlinkByUserId(socialId);
            } catch (Exception e) {
                log.warn("Kakao unlink failed after withdrawal. socialId={}", socialId, e);
            }
        });
    }
}