package com.ticket.core.domain.hold;

import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberFinder;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceFinder;
import com.ticket.core.domain.performanceseat.PerformanceSeat;
import com.ticket.core.domain.performanceseat.PerformanceSeatFinder;
import com.ticket.core.domain.seat.Seat;
import com.ticket.core.domain.seat.SeatRepository;
import com.ticket.core.enums.HoldState;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * v2: Redis Lua script 활용한 선점, 선점 해제
 */
@Service
@RequiredArgsConstructor
public class HoldRedisService implements HoldService {
    private static final Logger log = LoggerFactory.getLogger(HoldRedisService.class);
    private final RedissonClient redissonClient;
    private final MemberFinder memberFinder;
    private final PerformanceFinder performanceFinder;
    private final SeatRepository seatRepository;
    private final PerformanceSeatFinder performanceSeatFinder;
    private final HoldRepository holdRepository;
    private final HoldItemRepository holdItemRepository;

    private static final String HOLD_SCRIPT = """
        local memberId = ARGV[1]
        local performanceId = ARGV[2]
        local ttlSeconds = tonumber(ARGV[3])
        
        for i, key in ipairs(KEYS) do
            local existing = redis.call('GET', key)
            if existing then
                return {0, key, existing}
            end
        end
        
        local holdData = cjson.encode({
            memberId = memberId,
            performanceId = performanceId,
            holdAt = redis.call('TIME')[1]
        })
        
        for i, key in ipairs(KEYS) do
            redis.call('SET', key, holdData, 'EX', ttlSeconds)
        end
        
        return {1}
        """;

    private static final String RELEASE_SCRIPT = """
        local memberId = ARGV[1]
        local releasedKeys = {}
        
        for i, key in ipairs(KEYS) do
            local existing = redis.call('GET', key)
            if existing then
                local data = cjson.decode(existing)
                if data.memberId == memberId then
                    redis.call('DEL', key)
                    table.insert(releasedKeys, key)
                end
            end
        end
        
        return releasedKeys
        """;

    @Transactional
    public Long hold(final Long memberId, final NewHold newHold) {
        //여기 밑에 select 하는 것들이 뭔가 성능상 나빠보이는데, 이 방법 밖에 없나?
        final Member foundMember = memberFinder.find(memberId);
        final Performance foundPerformance = performanceFinder.findOpenPerformance(newHold.getPerformanceId());
        final List<Seat> foundSeats = seatRepository.findByIdIn(newHold.getSeatIds());
        final List<PerformanceSeat> foundPerformanceSeats = performanceSeatFinder.findAllByPerformanceAndSeatIn(foundPerformance, foundSeats);
        if (foundPerformanceSeats.isEmpty()) {
            throw new CoreException(ErrorType.NOT_FOUND_DATA);
        }
        final RScript script = redissonClient.getScript(StringCodec.INSTANCE);

        final List<Long> seatIds = foundSeats.stream().map(Seat::getId).toList();
        final List<Object> keys = seatIds.stream()
                .map(seatId -> "seat:hold:{perf:" + foundPerformance.getId() + "}:" + seatId)
                .collect(Collectors.toList());

        final List<Object> result = script.eval(
                RScript.Mode.READ_WRITE,
                HOLD_SCRIPT,
                RScript.ReturnType.MULTI,
                keys,
                foundMember.getId().toString(),
                foundPerformance.getId().toString(),
                foundPerformance.getHoldTime().toString()
        );

        Long success = (Long) result.get(0);
        log.info("success = {}", success);
        if (success == 0L) {
            String failedKey = (String) result.get(1);
            String existingHolder = (String) result.get(2);
            log.info("failedKey = {}, existingHolder = {}", failedKey, existingHolder);
            throw new CoreException(ErrorType.SEAT_ALREADY_HOLD);
        }

        final Hold hold = new Hold(foundMember, LocalDateTime.now().plusSeconds(foundPerformance.getHoldTime()), HoldState.ACTIVE);
        final Hold savedHold = holdRepository.save(hold);
        final List<HoldItem> holdItems = foundPerformanceSeats.stream()
                .map(ps -> new HoldItem(savedHold, ps, ps.getPrice()))
                .toList();
        holdItemRepository.saveAll(holdItems);
        //todo 이력용으로 hold에 저장하긴 했는데, hold의 state도 관리를 해야하나? 해줘야겠지. 성공했는지 실패했는지 알 수가 없으니.
        //선점이 결제까지 완료 되었는지, 아니면 결제에서 실패했는지 시간초과인지 등
        return hold.getId();
    }

    /**
     * 결제 성공 시 선점 해제
     * 여러 좌석 동시 해제
     * @return 해제된 좌석 키 목록
     */
    public List<String> releaseSeats(Long performanceId, List<Long> seatIds, Long memberId) {
        RScript script = redissonClient.getScript(StringCodec.INSTANCE);

        List<Object> keys = seatIds.stream()
                .map(seatId -> "seat:hold:{perf:" + performanceId + "}:" + seatId)
                .collect(Collectors.toList());

        return script.eval(
                RScript.Mode.READ_WRITE,
                RELEASE_SCRIPT,
                RScript.ReturnType.MULTI,
                keys,
                memberId.toString()
        );
    }

}
