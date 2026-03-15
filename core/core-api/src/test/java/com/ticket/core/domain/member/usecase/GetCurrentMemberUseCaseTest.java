package com.ticket.core.domain.member.usecase;

import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberFinder;
import com.ticket.core.domain.member.vo.Email;
import com.ticket.core.enums.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCurrentMemberUseCaseTest {

    @Mock
    private MemberFinder memberFinder;

    @InjectMocks
    private GetCurrentMemberUseCase useCase;

    @Test
    void 현재_회원_정보를_반환한다() {
        Member member = mock(Member.class);
        when(member.getId()).thenReturn(3L);
        when(member.getEmail()).thenReturn(Email.create("user@example.com"));
        when(member.getName()).thenReturn("홍길동");
        when(member.getRole()).thenReturn(Role.MEMBER);
        when(memberFinder.findActiveMemberById(3L)).thenReturn(member);

        GetCurrentMemberUseCase.Output output = useCase.execute(new GetCurrentMemberUseCase.Input(3L));

        assertThat(output.memberId()).isEqualTo(3L);
        assertThat(output.email()).isEqualTo("user@example.com");
        assertThat(output.name()).isEqualTo("홍길동");
        assertThat(output.role()).isEqualTo("MEMBER");
    }

    @Test
    void 이메일이_null이면_빈문자열로_반환한다() {
        Member member = mock(Member.class);
        when(member.getId()).thenReturn(3L);
        when(member.getEmail()).thenReturn(null);
        when(member.getName()).thenReturn("홍길동");
        when(member.getRole()).thenReturn(Role.MEMBER);
        when(memberFinder.findActiveMemberById(3L)).thenReturn(member);

        GetCurrentMemberUseCase.Output output = useCase.execute(new GetCurrentMemberUseCase.Input(3L));

        assertThat(output.email()).isEmpty();
    }

    @Test
    void memberId가_null이면_예외를_던진다() {
        assertThatThrownBy(() -> new GetCurrentMemberUseCase.Input(null))
                .isInstanceOf(NullPointerException.class);
    }
}
