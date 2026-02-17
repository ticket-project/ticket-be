package com.ticket.core.aop;

import com.ticket.core.support.CustomSpringELParser;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@Aspect
@RequiredArgsConstructor
public class DistributedLockAop {
    private static final String REDISSON_LOCK_PREFIX = "LOCK:";
    private static final Logger log = LoggerFactory.getLogger(DistributedLockAop.class);

    private final RedissonClient redissonClient;
    private final AopForTransaction aopForTransaction;

    @Around("@annotation(com.ticket.core.aop.DistributedLock)")
    public Object around(final ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("aop 시작");
        final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        final DistributedLock distributedLock = signature.getMethod().getAnnotation(DistributedLock.class);
        List<String> keys = CustomSpringELParser.getDynamicValue(distributedLock.prefix() + "-" + REDISSON_LOCK_PREFIX, signature.getParameterNames(), joinPoint.getArgs(), distributedLock.dynamicKey());
        final List<String> sortedKey = keys.stream().sorted().toList();
        final RLock rLock = generateLock(sortedKey);
        log.info("sortedKey={}", sortedKey);
        try {
            final boolean available = rLock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit());
            if (!available) {
                log.warn("락 획득 실패 - sortedKey = {}", sortedKey);
                throw new IllegalStateException("락 획득 실패");
            }
            return aopForTransaction.proceed(joinPoint);
        } catch (InterruptedException e) {
            throw new InterruptedException("락 획득 실패");
        } finally {
            try {
                rLock.unlock();
            } catch (IllegalMonitorStateException e) {
                log.error("락 이미 해제됨 - sortedKey = {}", sortedKey, e);
            }
        }
    }

    private RLock generateLock(final List<String> keys) {
        if (keys.size() == 1) {
            return redissonClient.getLock(keys.getFirst());
        }
        RLock[] locks = keys.stream()
                .map(redissonClient::getLock)
                .toArray(RLock[]::new);
        return redissonClient.getMultiLock(locks);
    }
}
