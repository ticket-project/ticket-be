package com.ticket.core.api.controller;

import com.ticket.core.api.controller.docs.PerformerAlertControllerDocs;
import com.ticket.core.config.security.MemberPrincipal;
import com.ticket.core.domain.performeralert.command.SubscribePerformerAlertUseCase;
import com.ticket.core.domain.performeralert.command.UnsubscribePerformerAlertUseCase;
import com.ticket.core.domain.performeralert.query.GetMyPerformerAlertsUseCase;
import com.ticket.core.support.exception.AuthException;
import com.ticket.core.support.exception.ErrorType;
import com.ticket.core.support.response.ApiResponse;
import com.ticket.core.support.response.SliceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/performer-alerts")
@RequiredArgsConstructor
public class PerformerAlertController implements PerformerAlertControllerDocs {

    private final SubscribePerformerAlertUseCase subscribePerformerAlertUseCase;
    private final UnsubscribePerformerAlertUseCase unsubscribePerformerAlertUseCase;
    private final GetMyPerformerAlertsUseCase getMyPerformerAlertsUseCase;

    @Override
    @PostMapping("/performers/{performerId}")
    public ApiResponse<SubscribePerformerAlertUseCase.Output> subscribe(
            final MemberPrincipal memberPrincipal,
            @PathVariable final Long performerId
    ) {
        final Long memberId = requireMemberId(memberPrincipal);
        final SubscribePerformerAlertUseCase.Input input = new SubscribePerformerAlertUseCase.Input(memberId, performerId);
        return ApiResponse.success(subscribePerformerAlertUseCase.execute(input));
    }

    @Override
    @DeleteMapping("/performers/{performerId}")
    public ApiResponse<UnsubscribePerformerAlertUseCase.Output> unsubscribe(
            final MemberPrincipal memberPrincipal,
            @PathVariable final Long performerId
    ) {
        final Long memberId = requireMemberId(memberPrincipal);
        final UnsubscribePerformerAlertUseCase.Input input = new UnsubscribePerformerAlertUseCase.Input(memberId, performerId);
        return ApiResponse.success(unsubscribePerformerAlertUseCase.execute(input));
    }

    @Override
    @GetMapping("/me")
    public ApiResponse<SliceResponse<GetMyPerformerAlertsUseCase.PerformerAlertSummary>> getMyAlerts(
            final MemberPrincipal memberPrincipal,
            @RequestParam(required = false) final String cursor,
            @RequestParam(defaultValue = "20") final int size
    ) {
        final Long memberId = requireMemberId(memberPrincipal);
        final GetMyPerformerAlertsUseCase.Input input = new GetMyPerformerAlertsUseCase.Input(memberId, cursor, size);
        final GetMyPerformerAlertsUseCase.Output output = getMyPerformerAlertsUseCase.execute(input);
        return ApiResponse.success(SliceResponse.from(output.performers(), output.nextCursor()));
    }

    private Long requireMemberId(final MemberPrincipal memberPrincipal) {
        if (memberPrincipal == null || memberPrincipal.getMemberId() == null) {
            throw new AuthException(ErrorType.AUTHENTICATION_ERROR);
        }
        return memberPrincipal.getMemberId();
    }
}
