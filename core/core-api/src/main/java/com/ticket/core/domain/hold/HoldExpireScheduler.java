package com.ticket.core.domain.hold;

import com.ticket.core.domain.performanceseat.PerformanceSeat;
import com.ticket.core.domain.performanceseat.PerformanceSeatRepository;
import com.ticket.core.enums.PerformanceSeatState;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Hold 만료 스케줄러.
 * HOLDING 상태는 DB가 아닌 Redis에서 관리하므로,
 * Redis TTL 만료로 자동 처리됩니다.
 * 이 스케줄러는 비활성화 상태입니다.
 */
//@Component
@RequiredArgsConstructor
public class HoldExpireScheduler {

    private final PerformanceSeatRepository performanceSeatRepository;

    @Transactional
    @Scheduled(fixedRate = 60000)
    public void expireSeatHolds() {
        // HOLDING 상태는 Redis에서만 관리하므로 DB 조회 불필요.
        // Redis TTL 만료 시 자동으로 HOLDING 해제됨.
        // 필요 시 Redis 기반으로 재구현.
    }
}
