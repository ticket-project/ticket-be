package com.ticket.core.api.controller.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "로그인 요청")
public class LoginMemberRequest {

    @Schema(description = "로그인 아이디(이메일)", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonAlias({"id", "loginId"})
    private String email;

    @Schema(description = "비밀번호", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

}
