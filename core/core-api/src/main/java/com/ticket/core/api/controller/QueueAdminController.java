package com.ticket.core.api.controller;

import com.ticket.core.api.controller.docs.QueueAdminControllerDocs;
import com.ticket.core.api.controller.request.UpdateQueuePolicyRequest;
import com.ticket.core.api.controller.response.QueuePolicyResponse;
import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.domain.queue.support.QueuePolicyAdminService;
import com.ticket.core.enums.Role;
import com.ticket.core.support.exception.AuthException;
import com.ticket.core.support.exception.ErrorType;
import com.ticket.core.support.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/queue/performances")
@RequiredArgsConstructor
public class QueueAdminController implements QueueAdminControllerDocs {

    private final QueuePolicyAdminService queuePolicyAdminService;

    @Override
    @GetMapping("/{performanceId}/policy")
    public ApiResponse<QueuePolicyResponse> getPolicy(
            @PathVariable final Long performanceId,
            final MemberPrincipal memberPrincipal
    ) {
        assertAdmin(memberPrincipal);
        return ApiResponse.success(QueuePolicyResponse.from(queuePolicyAdminService.get(performanceId)));
    }

    @Override
    @PutMapping("/{performanceId}/policy")
    public ApiResponse<QueuePolicyResponse> updatePolicy(
            @PathVariable final Long performanceId,
            @Valid @RequestBody final UpdateQueuePolicyRequest request,
            final MemberPrincipal memberPrincipal
    ) {
        assertAdmin(memberPrincipal);
        final QueuePolicyAdminService.PolicyDetail detail = queuePolicyAdminService.upsert(
                performanceId,
                new QueuePolicyAdminService.UpdateCommand(
                        request.queueMode(),
                        request.queueLevel(),
                        request.maxActiveUsers(),
                        request.entryTokenTtlSeconds(),
                        request.preopenQueueStartAt(),
                        request.waitingRoomMessage(),
                        request.reason()
                )
        );
        return ApiResponse.success(QueuePolicyResponse.from(detail));
    }

    private void assertAdmin(final MemberPrincipal memberPrincipal) {
        if (memberPrincipal.getRole() != Role.ADMIN) {
            throw new AuthException(ErrorType.AUTHORIZATION_ERROR);
        }
    }
}
