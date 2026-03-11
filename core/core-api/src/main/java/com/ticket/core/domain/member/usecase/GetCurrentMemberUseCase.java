package com.ticket.core.domain.member.usecase;

import com.ticket.core.api.controller.response.MemberResponse;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetCurrentMemberUseCase {
    private final MemberFinder memberFinder;

    public Output execute(final Input input) {
        final Member findMember = memberFinder.findActiveMemberById(input.memberId());
        return new Output(new MemberResponse(findMember.getId(), findMember.getEmail().getEmail(), findMember.getName(), findMember.getRole().name()));
    }

    public record Input(Long memberId) {
    }

    public record Output(MemberResponse memberResponse) {
    }
}
