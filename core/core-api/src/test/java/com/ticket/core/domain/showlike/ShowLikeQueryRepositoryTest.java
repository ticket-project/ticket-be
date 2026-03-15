package com.ticket.core.domain.showlike;

import com.ticket.core.domain.show.meta.Region;
import com.ticket.core.domain.support.QueryRepositoryTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Import(ShowLikeQueryRepository.class)
class ShowLikeQueryRepositoryTest extends QueryRepositoryTestSupport {

    @Autowired
    private ShowLikeQueryRepository showLikeQueryRepository;

    private Long memberId;

    @BeforeEach
    void setUp() throws Exception {
        var member = persistMember("user@example.com", "홍길동");
        memberId = member.getId();

        var venue = persistVenue("올림픽홀", Region.SEOUL);
        var show1 = persistShow("첫 공연", venue, null, 10L, LocalDateTime.now().minusDays(5), LocalDateTime.now().plusDays(5));
        var show2 = persistShow("둘째 공연", venue, null, 20L, LocalDateTime.now().minusDays(5), LocalDateTime.now().plusDays(5));
        var show3 = persistShow("셋째 공연", venue, null, 30L, LocalDateTime.now().minusDays(5), LocalDateTime.now().plusDays(5));

        persistShowLike(member, show1);
        persistShowLike(member, show2);
        persistShowLike(member, show3);
        flushAndClear();
    }

    @Test
    void 찜한_공연을_최신순으로_조회한다() {
        var result = showLikeQueryRepository.findMyLikedShows(memberId, null, 2);

        assertThat(result.slice().getContent()).extracting("title")
                .containsExactly("셋째 공연", "둘째 공연");
        assertThat(result.nextCursor()).isNotBlank();
        assertThat(result.slice().hasNext()).isTrue();
    }

    @Test
    void 커서_이후의_찜한_공연을_조회한다() {
        var firstPage = showLikeQueryRepository.findMyLikedShows(memberId, null, 1);
        var secondPage = showLikeQueryRepository.findMyLikedShows(memberId, Long.parseLong(firstPage.nextCursor()), 1);

        assertThat(firstPage.slice().getContent()).extracting("title").containsExactly("셋째 공연");
        assertThat(secondPage.slice().getContent()).extracting("title").containsExactly("둘째 공연");
    }
}
