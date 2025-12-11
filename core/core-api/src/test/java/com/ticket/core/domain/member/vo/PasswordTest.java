package com.ticket.core.domain.member.vo;

import com.ticket.core.support.exception.CoreException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;

@SuppressWarnings("NonAsciiCharacters")
class PasswordTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "123", "abc"})
    void 비밀번호가_null이거나_공백이거나_4자_미만이면_실패한다(final String value) {
        //then
        Assertions.assertThatThrownBy(() -> Password.create(value)).isInstanceOf(CoreException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234", "abcd", "Pa$$w0rd"})
    void 유효한_비밀번호면_Password_생성에_성공한다(final String value) {
        assertThatCode(() -> Password.create(value)).doesNotThrowAnyException();
    }
}
