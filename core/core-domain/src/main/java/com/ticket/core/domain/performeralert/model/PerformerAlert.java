package com.ticket.core.domain.performeralert.model;

import com.ticket.core.domain.BaseEntity;
import com.ticket.core.domain.member.model.Member;
import com.ticket.core.domain.show.performer.Performer;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;

import java.util.Objects;

@Getter
@Entity
@Table(
        name = "PERFORMER_ALERTS",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_PERFORMER_ALERTS_MEMBER_PERFORMER", columnNames = {"member_id", "performer_id"})
        },
        indexes = {
                @Index(name = "IDX_PERFORMER_ALERTS_MEMBER_ID_ID", columnList = "member_id,id"),
                @Index(name = "IDX_PERFORMER_ALERTS_PERFORMER_ID", columnList = "performer_id")
        }
)
public class PerformerAlert extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performer_id", nullable = false)
    private Performer performer;

    protected PerformerAlert() {
    }

    public PerformerAlert(final Member member, final Performer performer) {
        this.member = Objects.requireNonNull(member, "member must not be null");
        this.performer = Objects.requireNonNull(performer, "performer must not be null");
    }
}
