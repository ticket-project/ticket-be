package com.ticket.core.domain.member.usecase;

import com.ticket.core.domain.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WithdrawCurrentMemberUseCase {

    private final MemberService memberService;

    public record Input(Long memberId) {}
    public record Output() {}

    public Output execute(final Input input) {
        memberService.withdraw(input.memberId());
        return new Output();
    }
}
