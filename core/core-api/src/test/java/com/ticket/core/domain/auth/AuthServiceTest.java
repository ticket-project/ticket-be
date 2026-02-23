package com.ticket.core.domain.auth;

import com.ticket.core.domain.member.AddMember;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberRepository;
import com.ticket.core.domain.member.vo.Email;
import com.ticket.core.domain.member.vo.EncodedPassword;
import com.ticket.core.domain.member.vo.RawPassword;
import com.ticket.core.enums.EntityStatus;
import com.ticket.core.enums.Role;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordService passwordService;

    @Nested
    class Register {

        @Test
        void shouldRegisterMemberWhenInputIsValid() {
            // given
            final AddMember addMember = new AddMember(Email.create("test@test.com"), RawPassword.create("1234"), "test", Role.MEMBER);
            when(passwordService.encode(any())).thenReturn("encoded");
            final Member savedMember = new Member(
                    addMember.getEmail(),
                    EncodedPassword.create(addMember.getRawPassword().getPassword()),
                    addMember.getName(),
                    addMember.getRole()
            );
            ReflectionTestUtils.setField(savedMember, "id", 1L);
            when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

            // when
            final Long id = authService.register(addMember);

            // then
            assertThat(id).isEqualTo(1L);
            verify(memberRepository).save(any(Member.class));
        }

        @Test
        void shouldThrowDuplicateEmailWhenEmailAlreadyExists() {
            // given
            final AddMember addMember = new AddMember(Email.create("test@test.com"), RawPassword.create("1234"), "test", Role.MEMBER);
            when(passwordService.encode(any())).thenReturn("encoded");
            when(memberRepository.save(any(Member.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

            // when, then
            assertThatThrownBy(() -> authService.register(addMember))
                    .isInstanceOf(CoreException.class)
                    .extracting(error -> ((CoreException) error).getErrorType())
                    .isEqualTo(ErrorType.MEMBER_DUPLICATE_EMAIL);
        }
    }

    @Nested
    class Login {

        @Test
        void shouldLoginWhenEmailAndPasswordMatch() {
            // given
            final Email email = Email.create("test@test.com");
            final RawPassword rawPassword = RawPassword.create("1234");
            final EncodedPassword encodedPassword = EncodedPassword.create("encoded(1234)");
            final Member member = new Member(email, encodedPassword, "test", Role.MEMBER);
            when(memberRepository.findByEmail_EmailAndStatus(email.getEmail(), EntityStatus.ACTIVE)).thenReturn(Optional.of(member));
            when(passwordService.matches(rawPassword, member.getEncodedPassword())).thenReturn(true);

            // when
            final Member loggedInMember = authService.login(email.getEmail(), rawPassword.getPassword());

            // then
            assertThat(loggedInMember.getEmail().getEmail()).isEqualTo("test@test.com");
            assertThat(loggedInMember.getName()).isEqualTo("test");
            assertThat(loggedInMember.getRole()).isEqualTo(Role.MEMBER);
        }

        @Test
        void shouldThrowWhenEmailDoesNotExist() {
            // given
            final Email email = Email.create("test@test.com");
            final RawPassword rawPassword = RawPassword.create("encoded(1234)");

            // when, then
            assertThatThrownBy(() -> authService.login(email.getEmail(), rawPassword.getPassword()))
                    .isInstanceOf(CoreException.class);
        }

        @Test
        void shouldThrowWhenPasswordDoesNotMatch() {
            // given
            final Email email = Email.create("test@test.com");
            final RawPassword rawPassword = RawPassword.create("1234");
            final EncodedPassword encodedPassword = EncodedPassword.create("encoded(1234)");
            final Member member = new Member(email, encodedPassword, "test", Role.MEMBER);
            when(memberRepository.findByEmail_EmailAndStatus(email.getEmail(), EntityStatus.ACTIVE)).thenReturn(Optional.of(member));
            when(passwordService.matches(rawPassword, encodedPassword)).thenReturn(false);

            // when, then
            assertThatThrownBy(() -> authService.login(email.getEmail(), rawPassword.getPassword()))
                    .isInstanceOf(CoreException.class);
        }

        @Test
        void shouldRejectPasswordLoginForSocialAccount() {
            // given
            final Email email = Email.create("test@test.com");
            final Member member = new Member(email, null, "test", Role.MEMBER);
            when(memberRepository.findByEmail_EmailAndStatus(email.getEmail(), EntityStatus.ACTIVE)).thenReturn(Optional.of(member));

            // when, then
            assertThatThrownBy(() -> authService.login(email.getEmail(), "1234"))
                    .isInstanceOf(CoreException.class)
                    .extracting(error -> ((CoreException) error).getErrorType())
                    .isEqualTo(ErrorType.MEMBER_NOT_MATCH_PASSWORD);
            verify(passwordService, never()).matches(any(), any());
        }
    }
}
