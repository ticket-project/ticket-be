package com.ticket.member.vo;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("NonAsciiCharacters")
class EmailTest {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "test", "test@test", "test@test."})
    void 올바르지_않은_이메일이면_Email_생성에_실패한다(final String email) {
        //then
        assertThatThrownBy(() -> new Email(email)).isInstanceOf(IllegalArgumentException.class);
    }

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
        assertThat(result.getEmail()).isEqualTo(email.trim());
    }

}
