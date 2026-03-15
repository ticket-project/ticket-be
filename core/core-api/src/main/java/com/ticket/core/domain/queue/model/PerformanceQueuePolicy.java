package com.ticket.core.domain.queue.model;

import com.ticket.core.domain.BaseEntity;
import com.ticket.core.domain.performance.Performance;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "PERFORMANCE_QUEUE_POLICY")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PerformanceQueuePolicy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_id", nullable = false, unique = true)
    private Performance performance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QueueMode queueMode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QueueLevel queueLevel;

    private Integer maxActiveUsers;

    private Integer entryTokenTtlSeconds;

    private LocalDateTime preopenQueueStartAt;

    @Column(length = 255)
    private String waitingRoomMessage;

    @Column(length = 255)
    private String reason;

    public PerformanceQueuePolicy(
            final Performance performance,
            final QueueMode queueMode,
            final QueueLevel queueLevel,
            final Integer maxActiveUsers,
            final Integer entryTokenTtlSeconds,
            final LocalDateTime preopenQueueStartAt,
            final String waitingRoomMessage,
            final String reason
    ) {
        this.performance = performance;
        this.queueMode = queueMode;
        this.queueLevel = queueLevel;
        this.maxActiveUsers = maxActiveUsers;
        this.entryTokenTtlSeconds = entryTokenTtlSeconds;
        this.preopenQueueStartAt = preopenQueueStartAt;
        this.waitingRoomMessage = waitingRoomMessage;
        this.reason = reason;
    }

    public void update(
            final QueueMode queueMode,
            final QueueLevel queueLevel,
            final Integer maxActiveUsers,
            final Integer entryTokenTtlSeconds,
            final LocalDateTime preopenQueueStartAt,
            final String waitingRoomMessage,
            final String reason
    ) {
        this.queueMode = queueMode;
        this.queueLevel = queueLevel;
        this.maxActiveUsers = maxActiveUsers;
        this.entryTokenTtlSeconds = entryTokenTtlSeconds;
        this.preopenQueueStartAt = preopenQueueStartAt;
        this.waitingRoomMessage = waitingRoomMessage;
        this.reason = reason;
    }
}
