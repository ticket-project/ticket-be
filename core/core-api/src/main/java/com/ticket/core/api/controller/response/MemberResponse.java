package com.ticket.core.api.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 정보 응답")
public class MemberResponse {

    @Schema(description = "회원 ID", example = "1")
    private final Long memberId;

    @Schema(description = "이메일 주소", example = "user@example.com")
    private final String email;

    @Schema(description = "회원 이름", example = "홍길동")
    private final String name;

    @Schema(description = "회원 권한", example = "MEMBER", allowableValues = {"MEMBER", "ADMIN"})
    private final String role;

    public MemberResponse(final Long memberId, final String email, final String name, final String role) {
        this.memberId = memberId;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }
}

