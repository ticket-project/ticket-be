package com.ticket.core.domain.queue.runtime;

import java.util.Optional;

public final class QueueRedisKey {

    private static final String WAITING_PREFIX = "queue:performance:";
    private static final String WAITING_SUFFIX = ":waiting";
    private static final String ACTIVE_SUFFIX = ":active";
    private static final String SEQ_SUFFIX = ":seq";
    private static final String ENTRY_PREFIX = "queue:entry:";
    private static final String TOKEN_PREFIX = "queue:token:";

    private QueueRedisKey() {
    }

    public static String waiting(final Long performanceId) {
        return WAITING_PREFIX + performanceId + WAITING_SUFFIX;
    }

    public static String active(final Long performanceId) {
        return WAITING_PREFIX + performanceId + ACTIVE_SUFFIX;
    }

    public static String sequence(final Long performanceId) {
        return WAITING_PREFIX + performanceId + SEQ_SUFFIX;
    }

    public static String entry(final String queueEntryId) {
        return ENTRY_PREFIX + queueEntryId;
    }

    public static String createToken(final Long performanceId, final String queueEntryId, final String tokenId) {
        return performanceId + ":" + queueEntryId + ":" + tokenId;
    }

    public static String tokenStorageKey(final String queueToken) {
        return TOKEN_PREFIX + queueToken;
    }

    public static Optional<TokenKey> tryParseTokenStorageKey(final String key) {
        if (key == null || !key.startsWith(TOKEN_PREFIX)) {
            return Optional.empty();
        }
        return tryParseToken(key.substring(TOKEN_PREFIX.length()));
    }

    public static Optional<TokenKey> tryParseToken(final String queueToken) {
        if (queueToken == null || queueToken.isBlank()) {
            return Optional.empty();
        }

        final String[] parts = queueToken.split(":", 3);
        if (parts.length != 3) {
            return Optional.empty();
        }

        try {
            return Optional.of(new TokenKey(Long.parseLong(parts[0]), parts[1], parts[2]));
        } catch (final NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    public record TokenKey(Long performanceId, String queueEntryId, String tokenId) {
    }
}
