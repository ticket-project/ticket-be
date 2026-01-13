package com.ticket.core.api.controller;

import com.ticket.core.domain.performanceseat.PerformanceSeat;
import com.ticket.core.domain.performanceseat.PerformanceSeatService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/performance-seats")
public class PerformanceSeatController {
    private final PerformanceSeatService performanceSeatService;

    public PerformanceSeatController(final PerformanceSeatService performanceSeatService) {
        this.performanceSeatService = performanceSeatService;
    }

    /**
     * 대기열을 타고 들어와서 이 api 호출해서 현재 예약 가능한 좌석 목록을 조회할 수 있어야 한다.
     */
    @GetMapping("/list")
    public void getPerformanceSeatList() {
        List<PerformanceSeat> availableSeats = performanceSeatService.getAllAvailableSeats();
    }

}
