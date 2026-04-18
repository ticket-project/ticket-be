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
                .orElseThrow(() -> new IllegalArgumentException("吏?먰븯吏 ?딅뒗 ?湲곗뿴 ?좏겙 留뚮즺 ?ㅼ엯?덈떎: " + expiredKey));
        final String queueToken = tokenKey.performanceId() + ":" + tokenKey.queueEntryId() + ":" + tokenKey.tokenId();

        queueAdmissionAdvancer.handleTokenExpired(
                tokenKey.performanceId(),
                QueueEntryId.from(tokenKey.queueEntryId()),
                queueToken
        );
        log.info("?湲곗뿴 ?좏겙 留뚮즺 ?대깽??泥섎━: performanceId={}, queueEntryId={}",
                tokenKey.performanceId(), tokenKey.queueEntryId());
    }
}
