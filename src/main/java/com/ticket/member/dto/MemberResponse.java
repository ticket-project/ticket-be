package com.ticket.member.dto;

import com.ticket.member.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberResponse {

    private String email;
    private String name;

    public MemberResponse(final String email, final String name) {
        this.email = email;
        this.name = name;
    }

    public static MemberResponse of(final Member member) {
        return new MemberResponse(member.getEmail().getEmail(), member.getName());
    }
}
