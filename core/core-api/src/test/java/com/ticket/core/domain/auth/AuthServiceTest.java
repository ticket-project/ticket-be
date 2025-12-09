package com.ticket.core.domain.auth;

import com.ticket.core.domain.member.AddMember;
import com.ticket.core.domain.member.PasswordPolicyValidator;
import com.ticket.core.enums.Role;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import com.ticket.storage.db.core.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordPolicyValidator passwordPolicyValidator;

    @Test
    void 회원가입시_중복_이메일이면_실패한다() {
        //given
        final AddMember addMember = new AddMember("test@test.com", "1234", "test", Role.MEMBER);
        when(memberRepository.existsByEmail(addMember.getEmail())).thenReturn(true);
        doNothing().when(passwordPolicyValidator).validateAdd(addMember.getPassword());
        //when

        //then
        assertThatThrownBy(() -> authService.register(addMember))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.DUPLICATE_EMAIL_ERROR.getMessage());
    }
}
