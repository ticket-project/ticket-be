package com.ticket.core.config;

import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        //todo 이후 시큐리티 설정 되면 수정 필요
        return Optional.of("test");
    }

}
