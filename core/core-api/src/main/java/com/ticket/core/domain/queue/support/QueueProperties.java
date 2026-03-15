package com.ticket.core.domain.queue.support;

import com.ticket.core.domain.queue.model.QueueLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.queue")
public class QueueProperties {

    private boolean enabledByDefault = true;
    private QueueLevel defaultLevel = QueueLevel.LEVEL_1;
    private int defaultMaxActiveUsers = 300;
    private Duration defaultEntryTokenTtl = Duration.ofMinutes(10);
    private Duration entryRetention = Duration.ofHours(1);
}
