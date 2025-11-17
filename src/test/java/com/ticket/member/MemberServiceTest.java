package com.ticket.member;

import com.ticket.exception.DuplicateEmailException;
import com.ticket.member.dto.MemberCreateRequest;
import com.ticket.member.dto.MemberResponse;
import com.ticket.member.repository.MemberRepository;
import com.ticket.member.service.MemberService;
import com.ticket.member.vo.Email;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
        MemberCreateRequest.MemberCreateCommand memberCreateCommand = new MemberCreateRequest.MemberCreateCommand("test@test.com", "1234", "ANONYMOUS");
        when(passwordEncoder.encode("1234")).thenReturn("ENC(1234)");
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));
        //when
        final MemberResponse response = memberService.register(memberCreateCommand);
        //then
        verify(passwordEncoder).encode("1234");
        verify(memberRepository).save(argThat(
                member ->
                        member.getEmail().equals(new Email("test@test.com"))
                        && member.getPassword().equals("ENC(1234)")
                        && member.getName().equals("ANONYMOUS"))
        );
        assertThat(response.getEmail()).isEqualTo(memberCreateCommand.getEmail());
        assertThat(response.getName()).isEqualTo(memberCreateCommand.getName());
    }

    @Test
    void 이미_존재하는_이메일이면_회원가입에_실패한다() {
        //given
        MemberCreateRequest.MemberCreateCommand memberCreateCommand = new MemberCreateRequest.MemberCreateCommand("test@test.com", "1234", "ANONYMOUS");
        when(memberRepository.existsByEmailAddress("test@test.com")).thenReturn(true);
        //then
        assertThatThrownBy(() -> memberService.register(memberCreateCommand)).isInstanceOf(DuplicateEmailException.class);
        verify(memberRepository, times(1)).existsByEmailAddress("test@test.com");
        verify(memberRepository, never()).save(any(Member.class));
        verify(passwordEncoder, never()).encode(anyString());

    }
}
