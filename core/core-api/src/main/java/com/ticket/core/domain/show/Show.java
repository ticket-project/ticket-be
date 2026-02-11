package com.ticket.core.domain.show;


import com.ticket.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "SHOWS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Show extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String subTitle;

    private String info;

    private LocalDate startDate;

    private LocalDate endDate;

    private Long viewCount;

    @Enumerated(EnumType.STRING)
    private SaleType saleType;

    private LocalDate saleStartDate;

    private LocalDate saleEndDate;

    private String image;

    @Enumerated(EnumType.STRING)
    private Region region;

    private String venue;

    private Integer runningMinutes;

    @ManyToOne(fetch = FetchType.LAZY)
    private Performer performer;

    public Show(final String title, final String subTitle, final String info, final LocalDate startDate, final LocalDate endDate, final Long viewCount, final SaleType saleType, final LocalDate saleStartDate, final LocalDate saleEndDate, final String image, final Region region, final String venue, final Performer performer, final Integer runningMinutes) {
        this.title = title;
        this.subTitle = subTitle;
        this.info = info;
        this.startDate = startDate;
        this.endDate = endDate;
        this.viewCount = viewCount;
        this.saleType = saleType;
        this.saleStartDate = saleStartDate;
        this.saleEndDate = saleEndDate;
        this.image = image;
        this.region = region;
        this.venue = venue;
        this.performer = performer;
        this.runningMinutes = runningMinutes;
    }

}
