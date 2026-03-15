package com.ticket.core.domain.member;

import com.ticket.core.domain.member.vo.Email;
import com.ticket.core.domain.member.vo.EncodedPassword;
import com.ticket.core.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
class MemberTest {

    @Test
    void 소셜회원을_생성하면_비밀번호없이_생성된다() {
        //given
        //when
        Member member = Member.createSocialMember(Email.create("social@example.com"), "홍길동", Role.MEMBER);

        //then
        assertThat(member.getEmail()).isEqualTo(Email.create("social@example.com"));
        assertThat(member.getName()).isEqualTo("홍길동");
        assertThat(member.getRole()).isEqualTo(Role.MEMBER);
        assertThat(member.getEncodedPassword()).isNull();
    }

    @Test
    void 회원탈퇴시_삭제시각과_탈퇴용_이메일을_설정한다() {
        //given
        Member member = new Member(
                Email.create("user@example.com"),
                EncodedPassword.create("encoded-password"),
                "홍길동",
                Role.MEMBER
        );
        ReflectionTestUtils.setField(member, "id", 7L);

        //when
        member.withdraw();

        //then
        assertThat(member.isDeleted()).isTrue();
        assertThat(member.getDeletedAt()).isNotNull();
        assertThat(member.getEmail().getEmail()).startsWith("deleted_7_").endsWith("@withdrawn.ticket");
        assertThat(member.getEncodedPassword()).isNull();
    }
}

