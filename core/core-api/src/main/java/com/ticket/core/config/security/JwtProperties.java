package com.ticket.core.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    private String issuer = "ticket";
    private String secretKey;
    private long accessTokenExpirationSeconds = 3600L;

}
