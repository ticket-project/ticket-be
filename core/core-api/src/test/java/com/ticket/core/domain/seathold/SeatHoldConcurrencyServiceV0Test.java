package com.ticket.core.domain.seathold;

import com.ticket.core.enums.Role;
import com.ticket.core.support.TestDataFactory;
import com.ticket.storage.db.core.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@SuppressWarnings("NonAsciiCharacters")
class SeatHoldConcurrencyServiceV0Test {

    private static final Logger log = LoggerFactory.getLogger(SeatHoldConcurrencyServiceV0Test.class);
    @Autowired private SeatHoldService seatHoldService;
    @Autowired private MemberRepository memberRepository;
    @Autowired private PerformanceRepository performanceRepository;
    @Autowired private PerformanceSeatRepository performanceSeatRepository;
    @Autowired private SeatHoldRepository seatHoldRepository;

    private MemberEntity savedMember;
    private List<MemberEntity> savedMembers;
    private PerformanceEntity savedPerformance;
    private List<PerformanceSeatEntity> savedPerformanceSeats;

    @BeforeEach
    void setUp() {
        savedMember = memberRepository.save(TestDataFactory.createMember());
        savedPerformance = performanceRepository.save(TestDataFactory.createPerformance());
        savedPerformanceSeats = performanceSeatRepository.saveAll(
                TestDataFactory.createAvailableSeats(savedPerformance.getId(), List.of(1L))
        );
        savedMembers = IntStream.range(0, 100)
                .mapToObj(i -> memberRepository.save(TestDataFactory.createMember("user" + i + "@test.com", "pw", "name", Role.MEMBER)))
                .toList();
    }

    @AfterEach
    void tearDown() {
        seatHoldRepository.deleteAllInBatch();
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
                    seatHoldService.hold(request);
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
        final long holdCount = seatHoldRepository.count();
        assertThat(holdCount).isEqualTo(1);
        }
    }
}
