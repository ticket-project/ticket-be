package com.ticket.core.domain.hold;

import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberRepository;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceRepository;
import com.ticket.core.domain.performanceseat.PerformanceSeatRepository;
import com.ticket.core.enums.Role;
import com.ticket.core.support.IntegrationBase;
import com.ticket.core.support.TestDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class SeatHoldConcurrencyServiceV0IntegrationTest extends IntegrationBase {

    private static final Logger log = LoggerFactory.getLogger(SeatHoldConcurrencyServiceV0IntegrationTest.class);
    @Autowired private HoldService holdService;
    @Autowired private MemberRepository memberRepository;
    @Autowired private PerformanceRepository performanceRepository;
    @Autowired private PerformanceSeatRepository performanceSeatRepository;

    private List<Member> savedMembers;
    private Performance savedPerformance;

    @BeforeEach
    void setUp() {
        memberRepository.save(TestDataFactory.createMember());
        savedPerformance = performanceRepository.save(TestDataFactory.createPerformance());
        performanceSeatRepository.saveAll(
                TestDataFactory.createAvailableSeats(savedPerformance.getId(), List.of(1L), LocalDateTime.now().plusSeconds(savedPerformance.getHoldTime()), 1L, "testHoldTokenUUID")
        );
        savedMembers = IntStream.range(0, 100)
                .mapToObj(i -> memberRepository.save(TestDataFactory.createMember("user" + i + "@test.com", "pw", "name", Role.MEMBER)))
                .toList();
    }

    @AfterEach
    void tearDown() {
        performanceSeatRepository.deleteAllInBatch();
        performanceRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Test
    void 재고가_1개일때_여러_요청이_동시에_들어오면_비관적_락에_의해_한_명만_선점한다() throws InterruptedException {
        // given
        final int threadCount = 100;
        try (final ExecutorService es = Executors.newVirtualThreadPerTaskExecutor()) {
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            int idx = i;
            es.submit(() -> {
                try {
                    ready.countDown();
                    start.await();
                    final NewSeatHold request = new NewSeatHold(
                            savedMembers.get(idx).getId(),
                            savedPerformance.getId(),
                            List.of(1L)
                    );
                    holdService.hold(request);
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
        assertThat(failCount.get()).isEqualTo(99);
        }
    }

}
