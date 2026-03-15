package com.ticket.core.domain.show.query;

import com.querydsl.core.BooleanBuilder;
import com.ticket.core.domain.show.meta.ShowSortKey;
import com.ticket.core.domain.show.query.model.ShowCursor;
import com.ticket.core.support.cursor.CursorCodec;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class ShowCursorSupportTest {

    @Mock
    private CursorCodec cursorCodec;

    @InjectMocks
    private ShowCursorSupport showCursorSupport;

    @Test
    void cursor가_비어있으면_where절을_건드리지_않는다() {
        BooleanBuilder where = new BooleanBuilder();

        showCursorSupport.applyCursor(where, null, popularDesc());

        assertThat(where.hasValue()).isFalse();
    }

    @Test
    void cursor_디코딩에_실패하면_INVALID_REQUEST_예외를_던진다() {
        when(cursorCodec.decode("broken")).thenThrow(new IllegalArgumentException("decode failed"));

        assertThatThrownBy(() -> showCursorSupport.applyCursor(new BooleanBuilder(), "broken", popularDesc()))
                .isInstanceOf(CoreException.class)
                .satisfies(thrown -> assertThat(((CoreException) thrown).getErrorType()).isEqualTo(ErrorType.INVALID_REQUEST));
    }

    @Test
    void cursor의_sort가_요청과_다르면_INVALID_REQUEST_예외를_던진다() {
        when(cursorCodec.decode("cursor")).thenReturn(new ShowCursor(ShowSortKey.LATEST, "DESC", "2026-03-15T10:00:00", 1L));

        assertThatThrownBy(() -> showCursorSupport.applyCursor(new BooleanBuilder(), "cursor", popularDesc()))
                .isInstanceOf(CoreException.class)
                .satisfies(thrown -> assertThat(((CoreException) thrown).getErrorType()).isEqualTo(ErrorType.INVALID_REQUEST));
    }

    @Test
    void cursor의_dir가_요청과_다르면_INVALID_REQUEST_예외를_던진다() {
        when(cursorCodec.decode("cursor")).thenReturn(new ShowCursor(ShowSortKey.POPULAR, "ASC", "10", 1L));

        assertThatThrownBy(() -> showCursorSupport.applyCursor(new BooleanBuilder(), "cursor", popularDesc()))
                .isInstanceOf(CoreException.class)
                .satisfies(thrown -> assertThat(((CoreException) thrown).getErrorType()).isEqualTo(ErrorType.INVALID_REQUEST));
    }

    @Test
    void cursor의_lastId가_없으면_INVALID_REQUEST_예외를_던진다() {
        when(cursorCodec.decode("cursor")).thenReturn(new ShowCursor(ShowSortKey.POPULAR, "DESC", "10", null));

        assertThatThrownBy(() -> showCursorSupport.applyCursor(new BooleanBuilder(), "cursor", popularDesc()))
                .isInstanceOf(CoreException.class)
                .satisfies(thrown -> assertThat(((CoreException) thrown).getErrorType()).isEqualTo(ErrorType.INVALID_REQUEST));
    }

    @Test
    void cursor의_lastValue가_없으면_INVALID_REQUEST_예외를_던진다() {
        when(cursorCodec.decode("cursor")).thenReturn(new ShowCursor(ShowSortKey.POPULAR, "DESC", " ", 1L));

        assertThatThrownBy(() -> showCursorSupport.applyCursor(new BooleanBuilder(), "cursor", popularDesc()))
                .isInstanceOf(CoreException.class)
                .satisfies(thrown -> assertThat(((CoreException) thrown).getErrorType()).isEqualTo(ErrorType.INVALID_REQUEST));
    }

    @Test
    void cursor의_lastValue가_정렬형식과_맞지_않으면_INVALID_REQUEST_예외를_던진다() {
        when(cursorCodec.decode("cursor")).thenReturn(new ShowCursor(ShowSortKey.LATEST, "DESC", "not-a-date", 1L));

        assertThatThrownBy(() -> showCursorSupport.applyCursor(new BooleanBuilder(), "cursor", latestDesc()))
                .isInstanceOf(CoreException.class)
                .satisfies(thrown -> assertThat(((CoreException) thrown).getErrorType()).isEqualTo(ErrorType.INVALID_REQUEST));
    }

    @Test
    void 올바른_cursor면_where절에_조건을_추가한다() {
        BooleanBuilder where = new BooleanBuilder();
        when(cursorCodec.decode("cursor")).thenReturn(new ShowCursor(ShowSortKey.POPULAR, "DESC", "10", 1L));

        showCursorSupport.applyCursor(where, "cursor", popularDesc());

        assertThat(where.hasValue()).isTrue();
    }

    @Test
    void 다음_cursor를_인코딩해_반환한다() {
        ShowCursor nextCursor = new ShowCursor(ShowSortKey.POPULAR, "DESC", "10", 1L);
        when(cursorCodec.encode(nextCursor)).thenReturn("encoded");

        String result = showCursorSupport.buildNextCursor(1L, ShowSortKey.POPULAR, Sort.Direction.DESC, "10");

        assertThat(result).isEqualTo("encoded");
        verify(cursorCodec).encode(nextCursor);
    }

    private ShowSortSupport.SortOrder popularDesc() {
        return new ShowSortSupport.SortOrder(ShowSortKey.POPULAR, Sort.Direction.DESC);
    }

    private ShowSortSupport.SortOrder latestDesc() {
        return new ShowSortSupport.SortOrder(ShowSortKey.LATEST, Sort.Direction.DESC);
    }
}
