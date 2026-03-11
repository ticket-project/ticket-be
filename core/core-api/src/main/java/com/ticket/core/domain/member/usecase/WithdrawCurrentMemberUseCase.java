package com.ticket.core.domain.member.usecase;

import com.ticket.core.domain.auth.oauth2.KakaoUnlinkService;
import com.ticket.core.domain.member.MemberWithdrawalTxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WithdrawCurrentMemberUseCase {

    private final MemberWithdrawalTxService memberWithdrawalTxService;
    private final KakaoUnlinkService kakaoUnlinkService;

    public record Input(Long memberId) {}
    public record Output() {}

    public Output execute(final Input input) {
        final List<String> kakaoSocialIds = memberWithdrawalTxService.withdraw(input.memberId);
        unlinkKakaoAccountsSafely(kakaoSocialIds);
        return new Output();
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
