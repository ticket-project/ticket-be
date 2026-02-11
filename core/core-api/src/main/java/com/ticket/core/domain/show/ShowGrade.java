package com.ticket.core.domain.show;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "SHOW_GRADES")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShowGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Show show;

    private String gradeCode;

    private String gradeName;

    private BigDecimal price;

    private Integer sortOrder;

}
