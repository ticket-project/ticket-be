package com.ticket.core.infra.lock;

import com.ticket.core.support.CustomSpringELParser;
import com.ticket.core.support.exception.CoreException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class DistributedLockAop {

    private static final String LOCK_PREFIX = "LOCK:";
    private static final Logger log = LoggerFactory.getLogger(DistributedLockAop.class);

    private final RedissonClient redissonClient;

    @Around("@annotation(com.ticket.core.infra.lock.DistributedLock)")
    public Object around(final ProceedingJoinPoint joinPoint) throws Throwable {
        final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        final DistributedLock distributedLock = signature.getMethod().getAnnotation(DistributedLock.class);
        final List<String> keys = CustomSpringELParser.getDynamicValue(
                lockPrefix(distributedLock.prefix()),
                signature.getParameterNames(),
                joinPoint.getArgs(),
                distributedLock.dynamicKey()
        ).stream().distinct().sorted().toList();
        final RLock lock = generateLock(keys);

        try {
            final boolean available = lock.tryLock(
                    distributedLock.waitTime(),
                    distributedLock.leaseTime(),
                    distributedLock.timeUnit()
            );
            if (!available) {
                log.warn("遺꾩궛 ???띾뱷???ㅽ뙣?덉뒿?덈떎. keys={}", keys);
                throw new CoreException(distributedLock.errorType(), resolveMessage(distributedLock));
            }
            return joinPoint.proceed();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CoreException(distributedLock.errorType(), resolveMessage(distributedLock));
        } finally {
            unlockQuietly(lock, keys);
        }
    }

    private String lockPrefix(final String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return LOCK_PREFIX;
        }
        return LOCK_PREFIX + prefix + ":";
    }

    private String resolveMessage(final DistributedLock distributedLock) {
        return distributedLock.message().isBlank()
                ? distributedLock.errorType().getDescription()
                : distributedLock.message();
    }

    private RLock generateLock(final List<String> keys) {
        if (keys.size() == 1) {
            return redissonClient.getLock(keys.getFirst());
        }
        final RLock[] locks = keys.stream()
                .map(redissonClient::getLock)
                .toArray(RLock[]::new);
        return redissonClient.getMultiLock(locks);
    }

    private void unlockQuietly(final RLock lock, final List<String> keys) {
        try {
            lock.unlock();
        } catch (final IllegalMonitorStateException e) {
            log.debug("遺꾩궛 ???댁젣 ?쒖젏???꾩옱 ?ㅻ젅?쒓? ?쎌쓣 ?뚯쑀?섍퀬 ?덉? ?딆뒿?덈떎. keys={}", keys, e);
        } catch (final RuntimeException e) {
            log.error("遺꾩궛 ???댁젣 以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎. keys={}", keys, e);
        }
    }
}
