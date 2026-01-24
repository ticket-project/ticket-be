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

    //장소 todo 향후 물리적 공연장 테이블 나와서 연관관계?
    private String place;

    protected Show() {}

    public Show(final String title, final String subTitle, final String info, final LocalDate startDate, final LocalDate endDate, final String place, final Long viewCount) {
        this.title = title;
        this.subTitle = subTitle;
        this.info = info;
        this.startDate = startDate;
        this.endDate = endDate;
        this.place = place;
        this.viewCount = viewCount;
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

    public String getPlace() {
        return place;
    }

    public Long getViewCount() {
        return viewCount;
    }
}
