package com.ticket.member.dto;

import com.ticket.member.PasswordPolicy;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MemberCreateRequest {

    @Email
    private String email;
    @NotBlank
    @Size(min = PasswordPolicy.MIN_LENGTH)
    private String password;
    private String name;

    public MemberCreateRequest() {
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public MemberCreateCommand toCommand() {
        return new MemberCreateCommand(email, password, name);
    }

    static public class MemberCreateCommand {
        private String email;
        private String password;
        private String name;

        public MemberCreateCommand(final String email, final String password, final String name) {
            this.email = email;
            this.password = password;
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public String getPassword() {
            return password;
        }

        public String getName() {
            return name;
        }
    }
}
