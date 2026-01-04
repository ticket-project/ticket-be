package com.ticket.core.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

import static org.assertj.core.api.Assertions.assertThat;

public class ConcurrentTestUtil {
    private static final Logger log = LoggerFactory.getLogger(ConcurrentTestUtil.class);

    public static void execute(final int threadCount, IntConsumer consumer) throws InterruptedException {
        try (final ExecutorService es = Executors.newVirtualThreadPerTaskExecutor()) {
            CountDownLatch ready = new CountDownLatch(threadCount);
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);

            for (int i = 0; i < threadCount; i++) {
                final int idx = i;
                es.submit(() -> {
                    try {
                        ready.countDown();
                        start.await();
                        consumer.accept(idx);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        log.error("선점 실패: {}", e.getMessage(), e);
                        failCount.incrementAndGet();
                    } finally {
                        done.countDown();
                    }
                });
            }
            ready.await();
            start.countDown();
            done.await();
            assertThat(successCount.get()).isEqualTo(1);
            assertThat(failCount.get()).isEqualTo(threadCount - 1);
        }
    }
}
