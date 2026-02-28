package com.ticket.core.domain.showlike.usecase;

import com.ticket.core.api.controller.response.ShowLikeStatusResponse;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberRepository;
import com.ticket.core.domain.show.Show;
import com.ticket.core.domain.show.ShowJpaRepository;
import com.ticket.core.domain.showlike.ShowLike;
import com.ticket.core.domain.showlike.ShowLikeRepository;
import com.ticket.core.enums.EntityStatus;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AddShowLikeUseCase {

    private final ShowLikeRepository showLikeRepository;
    private final MemberRepository memberRepository;
    private final ShowJpaRepository showJpaRepository;

    public record Input(Long memberId, Long showId) {
    }

    public record Output(ShowLikeStatusResponse response) {
    }

    public Output execute(final Input input) {
        validateInput(input);

        final Member member = memberRepository.findByIdAndStatus(input.memberId(), EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA,
                        "회원을 찾을 수 없습니다. id=" + input.memberId()));

        if (showLikeRepository.existsByMember_IdAndShow_Id(input.memberId(), input.showId())) {
            return new Output(new ShowLikeStatusResponse(input.showId(), true, countLikes(input.showId())));
        }

        final Show show = showJpaRepository.findById(input.showId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA,
                        "공연을 찾을 수 없습니다. id=" + input.showId()));

        try {
            showLikeRepository.save(new ShowLike(member, show));
        } catch (DataIntegrityViolationException e) {
            // 동시성으로 이미 저장된 경우 멱등 성공으로 처리
            if (!showLikeRepository.existsByMember_IdAndShow_Id(input.memberId(), input.showId())) {
                throw e;
            }
        }

        return new Output(new ShowLikeStatusResponse(input.showId(), true, countLikes(input.showId())));
    }

    private void validateInput(final Input input) {
        if (input == null || input.memberId() == null || input.showId() == null) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "memberId와 showId는 필수입니다.");
        }
    }

    private long countLikes(final Long showId) {
        return showLikeRepository.countByShow_IdAndStatus(showId, EntityStatus.ACTIVE);
    }
}
