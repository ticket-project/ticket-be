package com.ticket.member;

import com.ticket.exception.DuplicateEmailException;
import com.ticket.member.dto.MemberCreateRequest;
import com.ticket.member.dto.MemberResponse;
import com.ticket.member.repository.MemberRepository;
import com.ticket.member.service.MemberService;
import com.ticket.member.vo.Email;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void 회원가입시_비밀번호는_암호화된_값으로_저장된다() {
        //given
        MemberCreateRequest.MemberCreateCommand command = new MemberCreateRequest.MemberCreateCommand("test@test.com", "1234", "ANONYMOUS");
        when(passwordEncoder.encode("1234")).thenReturn("ENC(1234)");
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));
        //when
        final MemberResponse response = memberService.register(command);
        //then
        verify(passwordEncoder).encode("1234");
        verify(memberRepository).save(argThat(
                member ->
                        member.getEmail().equals(new Email("test@test.com"))
                        && member.getPassword().equals("ENC(1234)")
                        && member.getName().equals("ANONYMOUS"))
        );
        assertThat(response.getEmail()).isEqualTo(command.getEmail());
        assertThat(response.getName()).isEqualTo(command.getName());
    }

    @Test
    void 이미_존재하는_이메일이면_회원가입에_실패한다() {
        //given
        MemberCreateRequest.MemberCreateCommand command = new MemberCreateRequest.MemberCreateCommand("test@test.com", "1234", "ANONYMOUS");
        when(memberRepository.existsByEmailAddress("test@test.com")).thenReturn(true);
        //then
        assertThatThrownBy(() -> memberService.register(command)).isInstanceOf(DuplicateEmailException.class);
        verify(memberRepository, times(1)).existsByEmailAddress("test@test.com");
        verify(memberRepository, never()).save(any(Member.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"123", "lhs"})
    void 비밀번호가_정책에_맞지않으면_회원가입에_실패한다(final String password) {
        //given
        MemberCreateRequest.MemberCreateCommand command = new MemberCreateRequest.MemberCreateCommand("test@test.com", password, "ANONYMOUS");
        //then
        assertThatThrownBy(() -> memberService.register(command)).isInstanceOf(IllegalArgumentException.class);

        verify(memberRepository, never()).save(any(Member.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    //todo
    //1. 비밀번호 테스트는 policytest에? 아니면 여기서?
//    2. 예외 관련 테스트, 클래스 만들기(예외처리기)
}
