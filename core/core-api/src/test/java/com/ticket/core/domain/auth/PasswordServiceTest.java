package com.ticket.core.domain.auth;

import com.ticket.core.domain.member.vo.EncodedPassword;
import com.ticket.core.domain.member.vo.RawPassword;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class PasswordServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordService passwordService;

    @Test
    void 비밀번호_인코딩을_위임한다() {
        //given
        when(passwordEncoder.encode("password123!")).thenReturn("encoded");

        //when
        String encoded = passwordService.encode("password123!");

        //then
        assertThat(encoded).isEqualTo("encoded");
        verify(passwordEncoder).encode("password123!");
    }

    @Test
    void 비밀번호_일치여부_검사를_위임한다() {
        //given
        RawPassword rawPassword = RawPassword.create("password123!");
        EncodedPassword encodedPassword = EncodedPassword.create("encoded");
        when(passwordEncoder.matches("password123!", "encoded")).thenReturn(true);

        //when
        boolean matched = passwordService.matches(rawPassword, encodedPassword);

        //then
        assertThat(matched).isTrue();
        verify(passwordEncoder).matches("password123!", "encoded");
    }
}

