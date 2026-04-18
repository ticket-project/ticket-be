package com.ticket.core.infra.redis;

import com.ticket.core.domain.queue.command.QueueAdmissionAdvancer;
import com.ticket.core.domain.queue.model.QueueEntryId;
import com.ticket.core.domain.queue.runtime.QueueRedisKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueTokenExpirationHandler implements RedisKeyExpirationHandler {

    private final QueueAdmissionAdvancer queueAdmissionAdvancer;

    @Override
    public boolean supports(final String expiredKey) {
        return QueueRedisKey.tryParseTokenStorageKey(expiredKey).isPresent();
    }

    @Override
    public void handle(final String expiredKey) {
        final QueueRedisKey.TokenKey tokenKey = QueueRedisKey.tryParseTokenStorageKey(expiredKey)
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 대기열 토큰 만료 키입니다: " + expiredKey));
        final String queueToken = tokenKey.performanceId() + ":" + tokenKey.queueEntryId() + ":" + tokenKey.tokenId();

        queueAdmissionAdvancer.handleTokenExpired(
                tokenKey.performanceId(),
                QueueEntryId.from(tokenKey.queueEntryId()),
                queueToken
        );
        log.info("대기열 토큰 만료 이벤트 처리: performanceId={}, queueEntryId={}",
                tokenKey.performanceId(), tokenKey.queueEntryId());
    }
}
