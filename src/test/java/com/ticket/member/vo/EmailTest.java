package com.ticket.member.vo;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "test", "test@test", "test@test."})
    void 올바르지_않은_이메일이면_Email_생성에_실패한다(final String email) {
        //then
        assertThatThrownBy(() -> new Email(email)).isInstanceOf(IllegalArgumentException.class);
    }

}