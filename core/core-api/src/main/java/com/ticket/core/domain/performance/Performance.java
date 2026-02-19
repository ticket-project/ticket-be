package com.ticket.core.domain.performance;

import com.ticket.core.domain.BaseEntity;
import com.ticket.core.domain.show.Show;
import com.ticket.core.enums.BookingStatus;
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

    private Long performanceNo;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime orderOpenTime;

    private LocalDateTime orderCloseTime;

    private int maxCanHoldCount;

    @Column
    private Integer holdTime = 300;

    @Enumerated(value = EnumType.STRING)
    private PerformanceState state;

    public Performance(final Show show, final Long performanceNo, final LocalDateTime startTime, final LocalDateTime endTime, final LocalDateTime orderOpenTime, final LocalDateTime orderCloseTime, final int maxCanHoldCount, final Integer holdTime, final PerformanceState state) {
        this.show = show;
        this.performanceNo = performanceNo;
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

    public BookingStatus calculateBookingStatus(final LocalDateTime now) {
        if (state != PerformanceState.OPEN) {
            return BookingStatus.CLOSED;
        }
        if (orderOpenTime == null || orderCloseTime == null) {
            return BookingStatus.CLOSED;
        }
        if (now.isBefore(orderOpenTime)) {
            return BookingStatus.BEFORE_OPEN;
        }
        if (now.isAfter(orderCloseTime)) {
            return BookingStatus.CLOSED;
        }
        return BookingStatus.ON_SALE;
    }
}
