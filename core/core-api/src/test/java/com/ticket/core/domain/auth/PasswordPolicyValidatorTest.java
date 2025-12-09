package com.ticket.core.domain.auth;

import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class PasswordPolicyValidatorTest {

    private final PasswordPolicyValidator passwordPolicyValidator = new PasswordPolicyValidator();

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "  \t", "123", "abc"})
    void 회원가입시_비밀번호가_null이거나_공백이거나_4자_이하면_실패한다(final String password) {
        //then
        assertThatThrownBy(() -> passwordPolicyValidator.validateAdd(password))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.INVALID_PASSWORD.getMessage());
    }
}