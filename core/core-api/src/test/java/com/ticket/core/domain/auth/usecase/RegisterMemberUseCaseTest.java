package com.ticket.core.domain.auth.usecase;

import com.ticket.core.domain.auth.AuthService;
import com.ticket.core.domain.member.vo.Email;
import com.ticket.core.domain.member.vo.RawPassword;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
class RegisterMemberUseCaseTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private RegisterMemberUseCase useCase;

    @Test
    void 입력값을_값객체로_변환해_회원가입을_호출한다() {
        when(authService.register(Email.create("user@example.com"), RawPassword.create("password123!"), "홍길동"))
                .thenReturn(11L);

        RegisterMemberUseCase.Output output =
                useCase.execute(new RegisterMemberUseCase.Input("user@example.com", "password123!", "홍길동"));

        assertThat(output.memberId()).isEqualTo(11L);
        verify(authService).register(Email.create("user@example.com"), RawPassword.create("password123!"), "홍길동");
    }
}
