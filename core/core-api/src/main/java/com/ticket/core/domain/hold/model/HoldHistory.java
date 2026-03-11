package com.ticket.core.domain.hold.model;

import com.ticket.core.domain.BaseEntity;
import com.ticket.core.enums.HoldReleaseReason;
import com.ticket.core.enums.HoldState;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "HOLD_HISTORY",
        indexes = {
                @Index(name = "IDX_HOLD_HISTORY_HOLD_TOKEN", columnList = "holdToken")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HoldHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String holdToken;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long performanceId;

    @Column(nullable = false)
    private Long performanceSeatId;

    @Column(nullable = false)
    private Long seatId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private HoldState status;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime releasedAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private HoldReleaseReason releaseReason;

    public HoldHistory(
            final String holdToken,
            final Long memberId,
            final Long performanceId,
            final Long performanceSeatId,
            final Long seatId,
            final LocalDateTime expiresAt
    ) {
        this.holdToken = holdToken;
        this.memberId = memberId;
        this.performanceId = performanceId;
        this.performanceSeatId = performanceSeatId;
        this.seatId = seatId;
        this.status = HoldState.ACTIVE;
        this.expiresAt = expiresAt;
    }

    public void confirm(final LocalDateTime now, final HoldReleaseReason reason) {
        this.status = HoldState.CONFIRMED;
        this.releasedAt = now;
        this.releaseReason = reason;
    }

    public void expire(final LocalDateTime now, final HoldReleaseReason reason) {
        this.status = HoldState.EXPIRED;
        this.releasedAt = now;
        this.releaseReason = reason;
    }

    public void cancel(final LocalDateTime now, final HoldReleaseReason reason) {
        this.status = HoldState.CANCELED;
        this.releasedAt = now;
        this.releaseReason = reason;
    }
}
