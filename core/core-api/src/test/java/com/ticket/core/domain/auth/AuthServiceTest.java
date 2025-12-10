package com.ticket.core.domain.auth;

import com.ticket.core.domain.member.AddMember;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.PasswordService;
import com.ticket.core.domain.member.vo.Email;
import com.ticket.core.domain.member.vo.Password;
import com.ticket.core.enums.Role;
import com.ticket.core.support.exception.CoreException;
import com.ticket.storage.db.core.MemberEntity;
import com.ticket.storage.db.core.MemberRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

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
            when(memberRepository.existsByEmail(any())).thenReturn(false);
            when(passwordService.encode(any())).thenReturn("encoded");
            final MemberEntity savedMemberEntity = new MemberEntity(
                    addMember.getEmailValue(),
                    addMember.getName(),
                    addMember.getPassword().getValue(),
                    addMember.getRole()
            );
            ReflectionTestUtils.setField(savedMemberEntity, "id", 1L);
            when(memberRepository.save(any(MemberEntity.class))).thenReturn(savedMemberEntity);
            //when
            final Long id = authService.register(addMember);
            //then
            assertThat(id).isEqualTo(1L);
            verify(memberRepository).save(any(MemberEntity.class));
        }

        @Test
        void 중복된_이메일이면_실패한다() {
            //given
            final AddMember addMember = new AddMember("test@test.com", "1234", "test", Role.MEMBER);
            when(memberRepository.existsByEmail(addMember.getEmailValue())).thenReturn(true);
            //then
            assertThatThrownBy(() -> authService.register(addMember))
                    .isInstanceOf(CoreException.class);

        }

    }

    @Nested
    class 로그인 {

        @Test
        void 올바른_입력값이면_성공한다() {
            //given
            final Email email = Email.create("test@test.com");
            final Password password = Password.create("encoded(1234)");
            final MemberEntity memberEntity = new MemberEntity(
                    email.getValue(),
                    password.getValue(),
                    "test",
                    Role.MEMBER
            );
            when(memberRepository.findByEmail(email.getValue())).thenReturn(Optional.of(memberEntity));
            when(passwordService.matches(password.getValue(), memberEntity.getPassword())).thenReturn(true);
            //when
            final Member loggedInMember = authService.login(email, password);
            //then
            assertThat(loggedInMember.getEmail()).isEqualTo("test@test.com");
            assertThat(loggedInMember.getName()).isEqualTo("test");
            assertThat(loggedInMember.getRole()).isEqualTo(Role.MEMBER);
        }
    }



}
