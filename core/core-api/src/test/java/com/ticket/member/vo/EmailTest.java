package com.ticket.member.vo;

import com.ticket.core.domain.member.vo.Email;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
class EmailTest {



    @ParameterizedTest
    @ValueSource(strings = {"test@test.com",
            "user@example.co.kr",
            "admin@domain.org",
            "user.name+tag@example.com"
    })
    void 올바른_이메일이면_Email_생성에_성공한다(final String email) {
        //when
        final Email result = new Email(email);
        //then
        assertThat(result.getValue()).isEqualTo(email.trim());
    }

}
