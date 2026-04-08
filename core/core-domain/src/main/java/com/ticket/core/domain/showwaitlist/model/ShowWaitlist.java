package com.ticket.core.domain.showwaitlist.model;

import com.ticket.core.domain.BaseEntity;
import com.ticket.core.domain.member.model.Member;
import com.ticket.core.domain.show.model.Show;
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
        name = "SHOW_WAITLISTS",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_SHOW_WAITLISTS_MEMBER_SHOW", columnNames = {"member_id", "show_id"})
        },
        indexes = {
                @Index(name = "IDX_SHOW_WAITLISTS_MEMBER_ID_ID", columnList = "member_id,id"),
                @Index(name = "IDX_SHOW_WAITLISTS_SHOW_ID", columnList = "show_id")
        }
)
public class ShowWaitlist extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    protected ShowWaitlist() {
    }

    public ShowWaitlist(final Member member, final Show show) {
        this.member = Objects.requireNonNull(member, "member must not be null");
        this.show = Objects.requireNonNull(show, "show must not be null");
    }
}
