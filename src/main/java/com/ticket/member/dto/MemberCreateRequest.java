package com.ticket.member.dto;

import com.ticket.member.PasswordPolicy;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberCreateRequest {

    @Email
    private String email;
    @NotBlank
    @Size(min = PasswordPolicy.MIN_LENGTH)
    private String password;
    private String name;

    public MemberCreateCommand toCommand() {
        return new MemberCreateCommand(email, password, name);
    }

    @AllArgsConstructor
    @Getter
    static public class MemberCreateCommand {
        private String email;
        private String password;
        private String name;
    }
}
