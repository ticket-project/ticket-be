package com.ticket.core.config;

import com.ticket.core.support.random.UuidSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;
import java.util.UUID;

@Configuration
public class CoreInfraConfig {

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("Asia/Seoul"));
    }

    @Bean
    public UuidSupplier uuidSupplier() {
        return UUID::randomUUID;
    }
}
