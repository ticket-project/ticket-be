package com.ticket.core.domain.auth;

import com.ticket.core.domain.member.AddMember;
import com.ticket.core.domain.member.Member;
import com.ticket.core.enums.Role;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import com.ticket.storage.db.core.MemberEntity;
import com.ticket.storage.db.core.MemberRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordService passwordService;

    @Nested
    class 회원가입 {

        @Test
        void 올바른_입력값이면_성공한다() {
            //given
            final AddMember addMember = new AddMember("test@test.com", "1234", "test", Role.MEMBER);
            when(memberRepository.existsByEmail(addMember.getEmailValue())).thenReturn(false);
            doNothing().when(passwordService).encode(addMember.getPassword());
            //when
            authService.register(addMember);
            //then
            final boolean isExist = memberRepository.existsByEmail(addMember.getEmailValue());
            assertThat(isExist).isTrue();

            final MemberEntity memberEntity = memberRepository.findByEmail(addMember.getEmailValue()).orElseThrow();
            assertThat(memberEntity.getEmail()).isEqualTo(addMember.getEmailValue());
            assertThat(memberEntity.getPassword()).isNotEqualTo("1234");
        }

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

    @Test
    void 로그인에_성공한다() {
        //given
        final AddMember addMember = new AddMember("test@test.com", "1234", "test", Role.MEMBER);
        authService.register(addMember);

        final String email = "test@test.com";
        final String password = "1234";
        //when
        final Member loggedInMember = authService.login(email, password);
        //then
        assertThat(loggedInMember.getEmailValue()).isEqualTo("test@test.com");
        assertThat(loggedInMember.getName()).isEqualTo("test");
    }

}
