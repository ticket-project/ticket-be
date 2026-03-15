package com.ticket.core.domain.member;

import com.ticket.core.domain.member.vo.Email;
import com.ticket.core.domain.member.vo.EncodedPassword;
import com.ticket.core.enums.Role;
import com.ticket.core.enums.SocialProvider;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
class MemberSocialAccountTest {

    @Test
    void 같은_소셜_ID인지_판단한다() {
        //given
        //when
        Member member = new Member(Email.create("user@example.com"), EncodedPassword.create("encoded"), "홍길동", Role.MEMBER);
        MemberSocialAccount account = MemberSocialAccount.create(member, SocialProvider.KAKAO, "kakao-123");

        //then
        assertThat(account.isSameSocialId("kakao-123")).isTrue();
        assertThat(account.isSameSocialId("other")).isFalse();
    }

    @Test
    void 연동계정_탈퇴시_삭제시각과_대체_socialId를_설정한다() {
        //given
        Member member = new Member(Email.create("user@example.com"), EncodedPassword.create("encoded"), "홍길동", Role.MEMBER);
        MemberSocialAccount account = MemberSocialAccount.create(member, SocialProvider.KAKAO, "kakao-123");
        ReflectionTestUtils.setField(account, "id", 11L);

        //when
        account.withdraw();

        //then
        assertThat(account.isDeleted()).isTrue();
        assertThat(account.getDeletedAt()).isNotNull();
        assertThat(account.getSocialId()).startsWith("deleted_11_");
        assertThat(account.getSocialId()).isNotEqualTo("kakao-123");
    }
}

