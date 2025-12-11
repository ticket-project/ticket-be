package com.ticket.core.domain.member.vo;

import com.ticket.core.support.exception.CoreException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("NonAsciiCharacters")
class EmailTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "test", "test@test", "test@test."})
    void 올바르지_않은_이메일이면_Email_생성에_실패한다(final String email) {
        //then
        assertThatThrownBy(() -> Email.create(email)).isInstanceOf(CoreException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"user@test.com", "user.name+tag@sub.domain.co"})
    void 올바른_이메일이면_Email_생성에_성공한다(final String email) {
        assertThatCode(() -> Email.create(email)).doesNotThrowAnyException();
    }
}
