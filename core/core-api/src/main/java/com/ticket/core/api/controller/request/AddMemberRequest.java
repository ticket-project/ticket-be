package com.ticket.core.api.controller.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.ticket.core.domain.member.AddMember;
import com.ticket.core.domain.member.vo.Email;
import com.ticket.core.domain.member.vo.RawPassword;
import com.ticket.core.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "회원가입 요청")
public class AddMemberRequest {

    @Schema(description = "로그인 아이디(이메일)", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonAlias({"id", "loginId"})
    private String email;

    @Schema(description = "비밀번호", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "회원 이름", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    public AddMemberRequest() {
    }

    public AddMember toAddMember() {
        return new AddMember(Email.create(email), RawPassword.create(password), name, Role.MEMBER);
    }
}
