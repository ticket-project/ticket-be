package com.ticket.core.domain.queue.model;

import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;

import java.util.Objects;

public final class QueueEntryId {

    private final String value;

    private QueueEntryId(final String value) {
        this.value = value;
    }

    public static QueueEntryId from(final String value) {
        final String normalized = normalize(value);
        validate(normalized);
        return new QueueEntryId(normalized);
    }

    private static String normalize(final String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private static void validate(final String value) {
        if (!value.isBlank()) {
            return;
        }
        throw new CoreException(ErrorType.INVALID_REQUEST, "queueEntryId는 비어 있을 수 없습니다.");
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(final Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        final QueueEntryId that = (QueueEntryId) object;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
