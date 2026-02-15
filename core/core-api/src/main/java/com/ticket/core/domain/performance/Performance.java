package com.ticket.core.domain.performance;

import com.ticket.core.domain.BaseEntity;
import com.ticket.core.domain.show.Show;
import com.ticket.core.enums.PerformanceState;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "PERFORMANCES")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Performance extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Show show;

    private Long roundNo;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime orderOpenTime;

    private LocalDateTime orderCloseTime;

    private int maxCanHoldCount;

    @Column
    private Integer holdTime = 300;

    @Enumerated(value = EnumType.STRING)
    private PerformanceState state;

    public Performance(final Show show, final Long roundNo, final LocalDateTime startTime, final LocalDateTime endTime, final LocalDateTime orderOpenTime, final LocalDateTime orderCloseTime, final int maxCanHoldCount, final Integer holdTime, final PerformanceState state) {
        this.show = show;
        this.roundNo = roundNo;
        this.startTime = startTime;
        this.endTime = endTime;
        this.orderOpenTime = orderOpenTime;
        this.orderCloseTime = orderCloseTime;
        this.maxCanHoldCount = maxCanHoldCount;
        this.holdTime = holdTime;
        this.state = state;
    }

    public boolean isOverCount(final long requestReserveCount) {
        return requestReserveCount > maxCanHoldCount;
    }
}
