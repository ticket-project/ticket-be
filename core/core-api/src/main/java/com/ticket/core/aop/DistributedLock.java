package com.ticket.core.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    /**
     * 도메인 구분용 prefix
     */
    String prefix() default "";

    /**
     * 락 키를 생성할 SpEL 표현식들.
     *
     * 결과가 Collection/배열이면 각 요소가 개별 락으로 처리됩니다.
     *
     * 예시:
     * - 단일: "#memberId"
     * - 멀티: {"#a", "#b"} 또는 "#request.getIds()"
     */
    String[] dynamicKey();

    /**
     * 락의 시간 단위
     */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    /**
     * 락을 기다리는 시간 (default - 5000ms)
     * 락 획득을 위해 waitTime 만큼 대기한다.
     */
    long waitTime() default 5000L;

    /**
     * 락 임대 시간 (default - 3000ms)
     * 락을 획득한 이후 leaseTime이 지나면 락을 해제한다.
     */
    long leaseTime() default 3000L;
}
