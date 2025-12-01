package com.ticket.core.api.controller.v1;

import com.ticket.core.api.controller.v1.request.ReserveSeatsRequest;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.performanceseat.PerformanceSeatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PerformanceSeatController {

    private final PerformanceSeatService performanceSeatService;

    public PerformanceSeatController(final PerformanceSeatService performanceSeatService) {
        this.performanceSeatService = performanceSeatService;
    }

    /**
     * 예매
     * @param request
     */
    @PostMapping
    public void reserveSeats(Member member, @RequestBody @Valid ReserveSeatsRequest request) {
        performanceSeatService.reserve(member, request.toNewPerformanceSeats(member));
    }
}
