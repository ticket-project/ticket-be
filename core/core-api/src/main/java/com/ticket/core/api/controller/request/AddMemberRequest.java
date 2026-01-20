package com.ticket.core.api.controller.request;

import com.ticket.core.domain.member.AddMember;
import com.ticket.core.domain.member.vo.Email;
import com.ticket.core.domain.member.vo.RawPassword;
import com.ticket.core.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "회원가입 요청")
public class AddMemberRequest {

    @Schema(description = "이메일 주소", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String email;

    @Schema(description = "비밀번호", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String password;

    @Schema(description = "회원 이름", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String name;

    public AddMemberRequest() {}

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public AddMember toAddMember() {
        return new AddMember(Email.create(email), RawPassword.create(password), name, Role.MEMBER);
    }
}

