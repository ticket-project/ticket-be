package com.ticket.member;

import com.ticket.member.vo.Email;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
public class MemberTest {

    @Test
    public void 정상_입력값이면_Member가_생성된다() {
        //given
        String email = "test@test.com";
        String password = "1234";
        String name = "ANONYMOUS";
        //when
        Member member = Member.register(new Email(email), password, name);
        //then
        assertThat(member.getEmail()).isEqualTo(new Email(email));
        assertThat(member.getPassword()).isEqualTo(password);
        assertThat(member.getName()).isEqualTo(name);
    }
}
