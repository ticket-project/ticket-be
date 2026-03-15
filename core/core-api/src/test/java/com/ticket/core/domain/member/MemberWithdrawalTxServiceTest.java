package com.ticket.core.domain.member;

import com.ticket.core.domain.member.vo.Email;
import com.ticket.core.domain.member.vo.EncodedPassword;
import com.ticket.core.enums.Role;
import com.ticket.core.enums.SocialProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class MemberWithdrawalTxServiceTest {

    @Mock
    private MemberFinder memberFinder;

    @Mock
    private MemberSocialAccountRepository memberSocialAccountRepository;

    @InjectMocks
    private MemberWithdrawalTxService memberWithdrawalTxService;

    @Test
    void 카카오_소셜_ID만_반환하고_회원과_연동계정을_탈퇴처리한다() {
        Member member = new Member(Email.create("user@example.com"), EncodedPassword.create("encoded"), "홍길동", Role.MEMBER);
        ReflectionTestUtils.setField(member, "id", 3L);
        MemberSocialAccount kakao = MemberSocialAccount.create(member, SocialProvider.KAKAO, "kakao-123");
        MemberSocialAccount google = MemberSocialAccount.create(member, SocialProvider.GOOGLE, "google-123");
        ReflectionTestUtils.setField(kakao, "id", 1L);
        ReflectionTestUtils.setField(google, "id", 2L);
        when(memberFinder.findActiveMemberById(3L)).thenReturn(member);
        when(memberSocialAccountRepository.findAllByMemberAndDeletedAtIsNull(member)).thenReturn(List.of(kakao, google));

        List<String> kakaoIds = memberWithdrawalTxService.withdraw(3L);

        assertThat(kakaoIds).containsExactly("kakao-123");
        assertThat(member.isDeleted()).isTrue();
        assertThat(kakao.isDeleted()).isTrue();
        assertThat(google.isDeleted()).isTrue();
        verify(memberFinder).findActiveMemberById(3L);
        verify(memberSocialAccountRepository).findAllByMemberAndDeletedAtIsNull(member);
    }
}
