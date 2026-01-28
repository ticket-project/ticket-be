package com.ticket.core.domain.show;


import com.ticket.core.domain.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "SHOWS")
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

    protected Show() {}

    public Show(final String title, final String subTitle, final String info, final LocalDate startDate, final LocalDate endDate, final Long viewCount, final SaleType saleType, final LocalDate saleStartDate, final LocalDate saleEndDate, final String image, final Region region, final String venue) {
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
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public String getInfo() {
        return info;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public SaleType getSaleType() {
        return saleType;
    }

    public LocalDate getSaleStartDate() {
        return saleStartDate;
    }

    public LocalDate getSaleEndDate() {
        return saleEndDate;
    }

    public String getImage() {
        return image;
    }

    public Region getRegion() {
        return region;
    }

    public String getVenue() {
        return venue;
    }
}
