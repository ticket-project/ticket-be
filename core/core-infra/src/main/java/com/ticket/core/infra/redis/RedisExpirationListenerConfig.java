package com.ticket.core.infra.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.Properties;

@Slf4j
@Configuration
public class RedisExpirationListenerConfig {

    private static final String EXPIRED_EVENT_PATTERN = "__keyevent@*__:expired";
    private static final String NOTIFY_KEYSPACE_EVENTS = "notify-keyspace-events";
    private static final String REQUIRED_NOTIFY_OPTIONS = "Ex";

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            final RedisConnectionFactory redisConnectionFactory,
            final RedisKeyExpirationListener redisKeyExpirationListener
    ) {
        final RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(redisKeyExpirationListener, new PatternTopic(EXPIRED_EVENT_PATTERN));
        return container;
    }

    @Bean
    public ApplicationRunner enableRedisKeyspaceNotifications(final RedisConnectionFactory redisConnectionFactory) {
        return args -> {
            RedisConnection connection = null;
            try {
                connection = redisConnectionFactory.getConnection();
                final Properties config = connection.serverCommands().getConfig(NOTIFY_KEYSPACE_EVENTS);
                final String current = config.getProperty(NOTIFY_KEYSPACE_EVENTS, "");
                if (supportsExpiredEvents(current)) {
                    return;
                }

                connection.serverCommands().setConfig(NOTIFY_KEYSPACE_EVENTS, REQUIRED_NOTIFY_OPTIONS);
                log.info("?덈뵒???ㅼ뒪?섏씠???뚮┝???쒖꽦?뷀뻽?듬땲?? ?ㅼ젙媛?{}", REQUIRED_NOTIFY_OPTIONS);
            } catch (final Exception e) {
                log.warn("?덈뵒???ㅼ뒪?섏씠???뚮┝ ?ㅼ젙???ㅽ뙣?덉뒿?덈떎. 留뚮즺 ?대깽??由ъ뒪?덇? ?숈옉?섏? ?딆쓣 ???덉뒿?덈떎.", e);
            } finally {
                if (connection != null) {
                    connection.close();
                }
            }
        };
    }

    private boolean supportsExpiredEvents(final String current) {
        return current.contains("E") && current.contains("x");
    }
}
