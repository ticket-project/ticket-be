package com.ticket.core.domain.show.venue;

import com.ticket.core.domain.BaseEntity;
import com.ticket.core.domain.show.meta.Region;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "VENUES")
public class Venue extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;

    @Enumerated(EnumType.STRING)
    private Region region;

    private String addressDetail;
    private String zipCode;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    private String phone;
    private String imageUrl;

    // --- 좌석 맵 SVG 레이아웃 ---
    private int viewBoxWidth;
    private int viewBoxHeight;
    private double seatDiameter;
    @Column(name = "GAP_X")
    private double gapX;
    @Column(name = "GAP_Y")
    private double gapY;
}
